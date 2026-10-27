import json
import re
from pathlib import Path

SIGNATURE = re.compile(r'^(?P<prefix>(?:extern )?(?:void\*|TeaVM_Array\*|void|int32_t|int64_t|float|double) )(?P<name>(?:meth_|initclass_|sysinitclass_|supertypeof_)\w+)\((?P<args>[^\n()]*)\)(?P<end> \{|;)', re.M)


def application_files(root, suffix):
    return sorted(path for package in ('probe', 'dev/java2gb/api')
                  for path in (root / 'classes' / package).glob('*' + suffix))


def compiler_flags(generated):
    plan = json.loads((Path(generated) / 'storage-plan.json').read_text())
    flags = plan.get('rom_arrays', {}).get('sdcc_flags', [])
    if flags not in ([], ['--nolospre']):
        raise ValueError('Unsupported generated SDCC requirement')
    return ['-Wf'+flag for flag in flags]


def annotate(text, fixed_methods=()):
    def replace(match):
        attribute = '__banked' if match['name'].startswith('meth_') and match['name'] not in fixed_methods else '__nonbanked'
        return f"{match['prefix']}{match['name']}({match['args']}) {attribute}{match['end']}"
    result = SIGNATURE.sub(replace, text)
    if re.search(r'&meth_\w+', text):
        raise ValueError('GBROM001: banked Java method pointers are unsupported')
    if re.search(r'^(?:extern )?[A-Za-z_][A-Za-z_0-9 *]* meth_\w+\([^\n]*\)(?: \{|;)', result, re.M):
        raise ValueError('GBROM001: unrecognized Java method signature')
    return result


def stage(staged, entry, resident_methods=(), placements=None):
    import shutil
    staged = Path(staged)
    output = staged / 'banked'
    if output.exists(): shutil.rmtree(output)
    shutil.copytree(staged, output, ignore=shutil.ignore_patterns('banked'))
    units = []
    placements = placements or {}
    fixed_classes = {'GameBoy', entry}
    fixed_methods = {m['name'] for source in application_files(output, '.c')
                     if source.stem in fixed_classes for m in SIGNATURE.finditer(source.read_text())} | set(resident_methods)
    for index, source in enumerate(application_files(output, '.c')):
        text = source.read_text()
        placement = '' if source.stem in fixed_classes else f'#pragma bank {placements.get(source.stem, 255)}\n' + f'BANKREF(gb_java_unit_{index})\n'
        source.write_text('#include <gb/gb.h>\n' + placement + annotate(text, fixed_methods))
        units.append(source)
    for header in application_files(output, '.h'):
        header.write_text(annotate(header.read_text(), fixed_methods))
    headers = [f'#include "{h.relative_to(output)}"' for h in application_files(output, '.h')]
    sources = ['#include "runtime.c"'] + headers
    sources += [f'#include "{source.relative_to(output)}"'
                for directory in ('primitives', 'arrays/primitives')
                for source in sorted((output / directory).glob('*.c'))]
    sources += ['#include "planned_entry.c"']
    (output / 'game.c').write_text('\n'.join(sources)+'\n')
    manifest = {'mapper':'MBC5','bank_size':16384,'calling_convention':'SDCC __banked',
                'units':[str(p.relative_to(output)) for p in units],
                'placements':placements, 'fixed_classes':sorted(fixed_classes), 'resident_methods':sorted(resident_methods), 'method_bodies_changed':False}
    (output / 'rom-plan.json').write_text(json.dumps(manifest,indent=2)+'\n')
    return output, [output / 'game.c', *units, *sorted((output / 'rom_arrays').glob('*.c'))]


def report(rom, names):
    rom = Path(rom)
    data = rom.read_bytes()
    if data[0x147] not in (0x19, 0x1B, 0x1C, 0x1D, 0x1E) or len(data) != 32768 << data[0x148]:
        raise ValueError('GBROM002: invalid MBC5 cartridge header or ROM size')
    has_ram = data[0x147] in (0x1B, 0x1D, 0x1E)
    if data[0x149] != (2 if has_ram else 0):
        raise ValueError('GBROM002: cartridge save-RAM header does not match the supported layout')
    bank_count = len(data) // 16384
    if bank_count > 256:
        raise ValueError('GBROM002: the current banked-call ABI supports at most 256 banks')
    areas = re.findall(r'^(_[A-Z][A-Z_0-9]*)\s+([0-9A-F]{8})\s+([0-9A-F]{8})\s+=',
                       rom.with_suffix('.map').read_text(), re.M)
    occupied = {0: 0x200}  
    for name, raw_start, raw_size in areas:
        start, size = int(raw_start, 16), int(raw_size, 16)
        if not size or start >= 0xc000 and start < 0x10000:
            continue
        if name.startswith('_CODE_') and name[6:].isdigit() and int(name[6:]) > 0:
            bank, offset = int(name[6:]), (start & 0xffff) - 0x4000
            if start >> 16 != bank:
                raise ValueError(f'GBROM002: {name} has an inconsistent bank address')
        elif name in {'_CODE', '_HOME', '_INITIALIZER', '_GSINIT', '_GSFINAL', '_LIT'}:
            bank, offset = 0, start
        else:
            continue
        if bank >= bank_count or offset < 0 or offset + size > 16384:
            raise ValueError(f'GBROM002: {name} exceeds its physical ROM bank')
        occupied[bank] = max(occupied.get(bank, 0), offset + size)
    linked_banks = {v for k,v in names.items() if k.startswith('___bank_gb_')}
    if not linked_banks.issubset(set(occupied)):
        raise ValueError('GBROM002: bank references do not match linked segments')
    result = {'mapper':'MBC5', 'cartridge_type':data[0x147], 'save_ram_bytes':8192 if data[0x149] == 2 else 0, 'rom_bytes':len(data), 'rom_banks':bank_count,
              'occupied_bytes':sum(occupied.values()),
              'free_bytes':len(data)-sum(occupied.values()),
              'banks':[{'bank':b, 'occupied_bytes':occupied.get(b,0),
                        'free_bytes':16384-occupied.get(b,0)} for b in range(bank_count)]}
    if data[0x147] in (0x1C, 0x1D, 0x1E):
        result['rumble'] = True
    rom.with_suffix('.rom-budget.json').write_text(json.dumps(result,indent=2)+'\n')
    return result
