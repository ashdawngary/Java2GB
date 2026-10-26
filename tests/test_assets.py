import hashlib
import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('asset_bundle', Path(__file__).resolve().parents[1] / '.local/tooling/asset_bundle.py')
assets = importlib.util.module_from_spec(spec)
spec.loader.exec_module(assets)


class StandaloneAssetTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.package = self.root / 'games/tetris'
        self.build = self.root / 'build'
        payloads = {
            'src/probe/TetrisData.java': b'package probe; public final class TetrisData {}\n',
            'resources/assets/metadata/reference-data.json': b'{"reference_sha1":"test-source"}\n',
            **{'resources/assets/data/' + name: bytes([0, 255, 3]) for name in
               ('screen-maps.bin', 'title-tiles.bin', 'gameplay-tiles.bin', 'audio-data.bin')},
        }
        files = {}
        for name, payload in payloads.items():
            path = self.package / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(payload)
            files[name] = dict(bytes=len(payload), sha256=hashlib.sha256(payload).hexdigest())
        self.manifest = self.package / 'resources/assets/manifest.json'
        self.manifest.write_text(json.dumps(dict(schema_version=2, game='tetris', files=files)))
        self.patch = patch.object(assets, 'PROJECTS', self.root / 'games')
        self.patch.start()
        self.addCleanup(self.patch.stop)

    def test_uses_source_files_directly_without_reference_or_cached_build(self):
        handwritten = self.package / 'src/probe/Game.java'
        handwritten.write_text('package probe; class Game {}')
        metadata = assets.prepare('tetris', self.build)
        self.assertEqual(metadata['reference_sha1'], 'test-source')
        self.assertEqual((self.build / 'screen-maps.bin').read_bytes(), bytes([0, 255, 3]))
        self.assertFalse((self.build / 'asset-src').exists())
        self.assertTrue((self.package / 'src/probe/TetrisData.java').exists())
        self.assertEqual(handwritten.read_text(), 'package probe; class Game {}')

    def test_missing_package_does_not_reuse_stale_compiler_inputs(self):
        assets.prepare('tetris', self.build)
        self.manifest.unlink()
        with self.assertRaisesRegex(RuntimeError, 'Missing source assets'):
            assets.prepare('tetris', self.build)

    def test_corruption_is_rejected_before_build_resources_change(self):
        assets.prepare('tetris', self.build)
        resource = self.build / 'reference-data.json'
        expected = resource.read_bytes()
        (self.package / 'src/probe/TetrisData.java').write_text('corrupt')
        with self.assertRaisesRegex(RuntimeError, 'checksum differs'):
            assets.prepare('tetris', self.build)
        self.assertEqual(resource.read_bytes(), expected)

    def test_missing_or_unlisted_asset_is_rejected(self):
        extra = self.package / 'resources/assets/data/unlisted.bin'
        extra.write_text('unexpected')
        with self.assertRaisesRegex(RuntimeError, 'inventory differs'):
            assets.prepare('tetris', self.build)
        extra.unlink()
        (self.package / 'resources/assets/data/screen-maps.bin').unlink()
        with self.assertRaisesRegex(RuntimeError, 'inventory differs'):
            assets.prepare('tetris', self.build)

    def extraction_fixture(self):
        assets.prepare('tetris', self.build)
        source = self.build / 'asset-src/probe'
        source.mkdir(parents=True)
        (source / 'TetrisData.java').write_text('package probe; public final class TetrisData { static final int VALUE=3; }')
        return source

    def test_extraction_refresh_preserves_handwritten_sources_and_removes_obsolete_assets(self):
        handwritten = self.package / 'src/probe/Game.java'
        handwritten.write_text('handwritten game code')
        obsolete = self.package / 'src/probe/ObsoleteData.java'
        obsolete.write_bytes(b'obsolete extracted asset')
        old = json.loads(self.manifest.read_text())
        old['files']['src/probe/ObsoleteData.java'] = dict(bytes=obsolete.stat().st_size,
            sha256=hashlib.sha256(obsolete.read_bytes()).hexdigest())
        self.manifest.write_text(json.dumps(old))
        extracted = self.extraction_fixture()
        assets.publish('tetris', self.build)
        self.assertEqual(handwritten.read_text(), 'handwritten game code')
        self.assertFalse(obsolete.exists())
        self.assertEqual((self.package / 'src/probe/TetrisData.java').read_bytes(),
                         (extracted / 'TetrisData.java').read_bytes())
        assets.compare_extraction('tetris', self.build)

    def test_extraction_cannot_overwrite_handwritten_sources(self):
        handwritten = self.package / 'src/probe/Game.java'
        handwritten.write_text('handwritten game code')
        extracted = self.extraction_fixture()
        (extracted / 'Game.java').write_text('unexpected extracted code')
        with self.assertRaisesRegex(RuntimeError, 'overwrite an unowned source'):
            assets.publish('tetris', self.build)
        self.assertEqual(handwritten.read_text(), 'handwritten game code')
