import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('rom_banking', Path(__file__).resolve().parents[1] / 'teavm/banking.py')
banking = importlib.util.module_from_spec(spec)
spec.loader.exec_module(banking)


class RomBankingValidationTests(unittest.TestCase):
    def test_api_units_are_staged_without_banking_runtime_declarations(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            files = {
                'classes/probe/Example.c': 'void meth_p_Example_main(void* args) {}\n',
                'classes/probe/Example.h': 'extern void meth_p_Example_main(void* args);\n',
                'classes/dev/java2gb/api/GameBoy.c': 'void meth_dja_GameBoy_init(void) {}\n',
                'classes/dev/java2gb/api/GameBoy.h': 'extern void meth_dja_GameBoy_init(void);\n',
                'classes/java/lang/Object.h': 'extern void meth_jl_Object__init_(void* self);\n',
            }
            for name, content in files.items():
                path = root / name
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(content)
            output, units = banking.stage(root, 'Example')
            self.assertIn(output / 'classes/probe/Example.c', units)
            self.assertIn(output / 'classes/dev/java2gb/api/GameBoy.c', units)
            self.assertIn('__nonbanked', (output / 'classes/dev/java2gb/api/GameBoy.h').read_text())
            self.assertEqual((output / 'classes/java/lang/Object.h').read_text(),
                             files['classes/java/lang/Object.h'])

    def test_plain_function_pointer_cannot_target_banked_java(self):
        with self.assertRaisesRegex(ValueError, 'GBROM001'):
            banking.annotate('void* callback = &meth_p_Test_run;\n')

    def test_unknown_calling_signature_is_rejected(self):
        with self.assertRaisesRegex(ValueError, 'GBROM001'):
            banking.annotate('extern TeaVM_String* meth_p_Test_string(void*);\n')

    def test_bank_overflow_and_wrong_bank_address_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            rom = Path(directory) / 'test.gb'
            data = bytearray(32768)
            data[0x147] = 0x19
            rom.write_bytes(data)
            for address, size in [('00014000', '00004001'), ('00024000', '00000010')]:
                with self.subTest(address=address, size=size):
                    rom.with_suffix('.map').write_text(f'_CODE_1 {address} {size} = 1. bytes\n')
                    with self.assertRaisesRegex(ValueError, 'GBROM002'):
                        banking.report(rom, {'___bank_gb_test': 1})
