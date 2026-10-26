import importlib.util
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('rom_banking', Path(__file__).resolve().parents[1] / 'teavm/banking.py')
banking = importlib.util.module_from_spec(spec)
spec.loader.exec_module(banking)


class RomBankingValidationTests(unittest.TestCase):
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
