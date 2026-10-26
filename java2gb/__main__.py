import argparse
import os
import subprocess
import sys

from .toolchain import ROOT, TOOLING

BUILD_TARGETS = {
    'sprite': ('teavm_plan.py', (), {'TEAVM_PLAN_ONLY': 'SpriteGame'}),
    'tetris': ('tetris.py', (), {}),
    'pokemon-pinball': ('pokemon_pinball.py', (), {}),
    'pocket-pinball': ('teavm_plan.py', (), {
        'TEAVM_ROM_BANKING': '1',
        'TEAVM_PLAN_ONLY': 'PinballPhysics,PinballReplay,PinballCamera,PinballPieces,PinballGate,PinballAnimations,PinballFeel,StaticStringProbe,PinballGame',
    }),
}
CHECK_TARGETS = {
    'core': 'test', 'unit': 'unit-test', 'compiler': 'compiler-check',
    'primitives': 'teavm-primitives', 'rom-arrays': 'teavm-rom-arrays',
    'mutability': 'teavm-mutability', 'nullness': 'nullness-check',
    'tetris': 'tetris-check', 'tetris-render': 'tetris-render-check', 'pokemon-pinball': 'pokemon-pinball-check',
}


def main(argv=None):
    parser = argparse.ArgumentParser(description='Command-line entry point for the Java → TeaVM IR → C → GBDK toolchain.')
    commands = parser.add_subparsers(dest='command', required=True)
    build = commands.add_parser('build', help='Build a checked-in Java entry through TeaVM and GBDK')
    build.add_argument('game', choices=BUILD_TARGETS)
    check = commands.add_parser('check', help='Run a TeaVM toolchain or game verification suite')
    check.add_argument('suite', choices=CHECK_TARGETS, default='core', nargs='?')
    args = parser.parse_args(argv)
    if args.command == 'build':
        script, flags, overrides = BUILD_TARGETS[args.game]
        env = dict(os.environ)
        for key in ('TEAVM_PLAN_ONLY', 'TEAVM_ROM_BANKING'):
            env.pop(key, None)
        env.update(overrides)
        command = [sys.executable, str(TOOLING / script), *flags]
    else:
        command = ['make', CHECK_TARGETS[args.suite]]
        env = None
    try:
        return subprocess.run(command, cwd=ROOT, env=env).returncode
    except OSError as error:
        print('java2gb: ' + str(error), file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
