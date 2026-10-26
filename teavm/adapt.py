from pathlib import Path
import shutil

ROOT = Path(__file__).resolve().parent


def primitive_array_files(generated):
    import json
    plan = generated / 'storage-plan.json'
    if plan.exists():
        kinds = {'[Z': 'boolean', '[B': 'byte', '[S': 'short', '[C': 'char', '[I': 'int'}
        names = sorted({kinds[a['type']] for a in json.loads(plan.read_text())['allocations']
                        if a['type'] in kinds})
    else:
        import re
        names = sorted({name for source in (generated / 'classes/probe').glob('*.[ch]')
                        for name in re.findall(r'arrays/primitives/(boolean|byte|short|char|int)\.h',
                                               source.read_text())})
    return [generated / directory / (name + extension)
            for name in names
            for directory in ('primitives', 'arrays/primitives')
            for extension in ('.c', '.h')]


def primitive_array_includes(output):
    return [f'#include "{source.relative_to(output)}"'
            for directory in ('primitives', 'arrays/primitives')
            for source in sorted((output / directory).glob('*.c'))]


def stage(generated, output, entry):
    generated, output = Path(generated), Path(output)
    if entry not in ("ObjectProbe", "SpriteGame"):
        raise ValueError("This adapter only supports the two checked-in probe entry points")
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)
    paths = list((generated / "classes/probe").glob("*"))
    paths += [generated / "classes/java/lang/Object.h", generated / "strings.h"]
    paths += primitive_array_files(generated)
    for source in paths:
        target = output / source.relative_to(generated)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
    for filename in ("runtime.h", "runtime.c"):
        shutil.copyfile(ROOT / "native/tiny" / filename, output / filename)
    allocator = output / "classes/org/teavm/runtime/Allocator.h"
    allocator.parent.mkdir(parents=True, exist_ok=True)
    allocator.write_text('#include "../../../../runtime.h"\n')
    sources = ['#include "runtime.c"']
    sources += primitive_array_includes(output)
    sources += [f'#include "{path.relative_to(output)}"' for path in sorted((output / "classes/probe").glob("*.c"))]
    sources += [f"void main(void) {{ meth_p_{entry}_main(NULL); for (;;) gb_wait_frame(); }}"]
    (output / "game.c").write_text("\n".join(sources) + "\n")
    return output / "game.c"


def stage_planned(generated, output, entry):
    import json
    generated, output = Path(generated), Path(output)
    plan = json.loads((generated / "storage-plan.json").read_text())
    if plan["entry"] != "probe." + entry:
        raise ValueError("Entry point does not match the allocation plan")
    if output.exists():
        shutil.rmtree(output)
    output.mkdir(parents=True)
    paths = list((generated / "classes/probe").glob("*"))
    paths += [generated / "classes/java/lang/Object.h", generated / "strings.h", generated / "planned_storage.c", generated / "planned_entry.c"]
    paths += primitive_array_files(generated)
    for source in paths:
        target = output / source.relative_to(generated)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
    strings = plan.get("static_strings", {}).get("required", False)
    if strings:
        for filename in ("rom_strings.h", "rom_strings_table.c"):
            shutil.copyfile(generated / filename, output / filename)
        shutil.copytree(generated / "rom_strings", output / "rom_strings")
        shutil.copyfile(ROOT / "native/tiny/rom_strings_runtime.c", output / "rom_strings_runtime.c")
    rom_arrays = plan.get("rom_arrays", {}).get("required", False)
    if rom_arrays:
        for filename in ("rom_arrays.h", "rom_arrays_table.c"):
            shutil.copyfile(generated / filename, output / filename)
        shutil.copytree(generated / "rom_arrays", output / "rom_arrays")
        shutil.copyfile(ROOT / "native/tiny/rom_arrays_runtime.c", output / "rom_arrays_runtime.c")
    header = (ROOT / "native/tiny/runtime.h").read_text()
    if strings:
        header += '\n#define GB_ROM_STRINGS 1\n#include "rom_strings.h"\n'
    if rom_arrays:
        header += '\n#define GB_ROM_ARRAYS 1\n#include "rom_arrays.h"\n'
    if plan.get("pools_enabled"):
        header += "\n#define GB_BOUNDED_POOLS 1\n" + (ROOT / "native/tiny/pool_roots.h").read_text()
        shutil.copyfile(ROOT / "native/tiny/pool_runtime.c", output / "pool_runtime.c")
    if plan.get("array_runtime_required") is False:
        header += "\n#define GB_PLAN_NO_ARRAYS 1\n"
    header += "\nvoid* gb_plan_allocate(uint16_t slot, void* cls, int32_t length);\n"
    header += "void* teavm_nullCheck(void* object);\nvoid teavm_gc_writeBarrier(void* object);\nvoid teavm_registerStaticGcRoots(void*** roots, int count);\n"
    header += "int32_t teavm_checkBounds(int32_t index, TeaVM_Array* array);\nint32_t teavm_checkUpperBound(int32_t index, TeaVM_Array* array);\n"
    (output / "runtime.h").write_text(header)
    shutil.copyfile(ROOT / "native/tiny/planned_runtime.c", output / "runtime.c")
    allocator = output / "classes/org/teavm/runtime/Allocator.h"
    allocator.parent.mkdir(parents=True, exist_ok=True)
    allocator.write_text('#include "../../../../runtime.h"\n')
    sources = ['#include "runtime.c"']
    sources += primitive_array_includes(output)
    sources += [f'#include "{path.relative_to(output)}"' for path in sorted((output / "classes/probe").glob("*.c"))]
    sources += ['#include "planned_entry.c"']
    sources += [f'#include "{path.relative_to(output)}"'
                for path in sorted((output / "rom_arrays").glob("*.c"))]
    (output / "game.c").write_text("\n".join(sources) + "\n")
    return output / "game.c"
