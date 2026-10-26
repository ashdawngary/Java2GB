import os
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent.parent
TOOLING = ROOT / ".local/tooling"


def executable(name, environment, candidates):
    override = os.environ.get(environment)
    if override:
        path = Path(override)
        if path.is_dir():
            path = path / 'bin' / name
        if not path.is_file():
            raise RuntimeError(f'{environment} does not point to {name}: {path}')
        return str(path)
    for path in candidates:
        if path.is_file():
            return str(path)
    system = shutil.which(name)
    if system and not (name == 'javac' and sys.platform == 'darwin' and system == '/usr/bin/javac'):
        return system
    raise RuntimeError(f'Cannot find {name}; run make bootstrap or set {environment}')


def javac_path():
    candidates = sorted((ROOT / '.tools').glob('jdk*/Contents/Home/bin/javac'))
    candidates += sorted((ROOT / '.tools').glob('jdk*/bin/javac'))
    return executable('javac', 'JAVA_HOME', candidates)
