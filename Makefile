PYTHON           ?= .venv/bin/python
BOOTSTRAP_PYTHON ?= python3
TOOLING          := .local/tooling
BUILD            := build

.DEFAULT_GOAL := help
.NOTPARALLEL:

.PHONY: help bootstrap clean build demo asm tetris pokemon-pinball pocket-pinball \
        pinball pokemon-pinball-build play tetris-play tetris-link \
        pokemon-pinball-play pinball-play teavm-play teavm-plan-play \
        teavm-pool-play test unit-test compiler-check tetris-check \
        tetris-render-check tetris-compat tetris-link-check pokemon-pinball-check \
        final-check nullness-check assets-check tetris-assets-extract \
        pokemon-pinball-assets-extract teavm-probe teavm-plan teavm-bank \
        teavm-primitives teavm-rom-arrays teavm-mutability array-access-benchmark

help:
	@printf '%s\n' \
	  'Java -> TeaVM IR -> C -> GBDK/SDCC -> Game Boy ROM' \
	  '' \
	  'Setup' \
	  '  bootstrap                  Install pinned local tools and dependencies' \
	  '  clean                      Remove generated build output' \
	  '' \
	  'Build and play' \
	  '  build                      Build all three games' \
	  '  tetris / tetris-play        Build / play Tetris' \
	  '  tetris-link                Play two linked Tetris cartridges' \
	  '  pokemon-pinball[-play]     Build / play Pokemon Pinball' \
	  '  pocket-pinball / pinball-play' \
	  '                             Build / play Pocket Pinball' \
	  '  demo / play                Build / play the sprite example' \
	  '  asm                        Emit inspectable SM83 assembly' \
	  '' \
	  'Verify' \
	  '  test                       Unit, compiler, JVM, and emulator checks' \
	  '  tetris-check               Full Tetris compatibility and performance checks' \
	  '  tetris-render-check        Deferred-render regression checks' \
	  '  pokemon-pinball-check      Pokemon Pinball reference comparisons' \
	  '  assets-check               Verify standalone source assets' \
	  '  final-check / nullness-check' \
	  '                             Java binding / nullness checks' \
	  '' \
	  'TeaVM development' \
	  '  teavm-plan / teavm-bank    Storage-planner checks, unbanked / banked' \
	  '  teavm-primitives / teavm-rom-arrays / teavm-mutability' \
	  '  array-access-benchmark     Measure generated array access costs' \
	  '' \
	  'Local build/check drivers live in ignored .local/tooling/.' \
	  'Original cartridges belong in ignored references/.'

bootstrap:
	$(BOOTSTRAP_PYTHON) $(TOOLING)/bootstrap.py

clean:
	rm -rf $(BUILD)

build: tetris pokemon-pinball pocket-pinball

demo:
	$(PYTHON) -m java2gb build sprite

asm: demo
	$(PYTHON) $(TOOLING)/emit_assembly.py -o $(BUILD)/teavm/sprite.asm

tetris:
	$(PYTHON) $(TOOLING)/tetris.py --game-only

pokemon-pinball:
	$(PYTHON) $(TOOLING)/pokemon_pinball.py --build-only

pocket-pinball:
	$(PYTHON) -m java2gb build pocket-pinball

pinball: pocket-pinball

pokemon-pinball-build: pokemon-pinball

play: demo
	$(PYTHON) -m pyboy $(BUILD)/teavm/planned-sprite.gb

tetris-play: tetris
	$(PYTHON) -m pyboy $(BUILD)/tetris/tetris.gb

tetris-link: tetris
	$(PYTHON) $(TOOLING)/tetris_link.py

pokemon-pinball-play: pokemon-pinball
	$(PYTHON) -m pyboy $(BUILD)/pokemon-pinball/pokemon-pinball.gbc

pinball-play: pocket-pinball
	$(PYTHON) -m pyboy $(BUILD)/teavm/planned-pinball.gb

teavm-play:
	$(PYTHON) -m pyboy $(BUILD)/teavm/tiny-sprite.gb

teavm-plan-play:
	$(PYTHON) -m pyboy $(BUILD)/teavm/planned-owned-sprite.gb

teavm-pool-play:
	$(PYTHON) -m pyboy $(BUILD)/teavm/planned-shooter.gb

test: unit-test compiler-check

unit-test:
	$(PYTHON) -m unittest discover -s tests -v

compiler-check:
	TEAVM_PLAN_ONLY=SpriteGame,Arithmetic $(PYTHON) $(TOOLING)/teavm_plan.py
	$(PYTHON) $(TOOLING)/compiler_checks.py

tetris-check:
	$(PYTHON) $(TOOLING)/tetris.py --check

tetris-render-check:
	$(PYTHON) $(TOOLING)/tetris.py --render-check

tetris-compat: tetris-check

tetris-link-check: tetris-check

pokemon-pinball-check:
	$(PYTHON) $(TOOLING)/pokemon_pinball.py --check

final-check:
	$(PYTHON) $(TOOLING)/tetris.py --final-only

nullness-check:
	$(PYTHON) $(TOOLING)/nullness_verify.py

assets-check:
	$(PYTHON) $(TOOLING)/asset_bundle.py verify tetris
	$(PYTHON) $(TOOLING)/asset_bundle.py verify pokemon-pinball

tetris-assets-extract:
	$(PYTHON) $(TOOLING)/asset_bundle.py extract tetris

pokemon-pinball-assets-extract:
	$(PYTHON) $(TOOLING)/asset_bundle.py extract pokemon-pinball

teavm-probe:
	$(PYTHON) $(TOOLING)/teavm_probe.py

teavm-plan:
	$(PYTHON) $(TOOLING)/teavm_plan.py

teavm-bank:
	TEAVM_ROM_BANKING=1 $(PYTHON) $(TOOLING)/teavm_plan.py

teavm-primitives:
	$(PYTHON) $(TOOLING)/teavm_primitives.py

teavm-rom-arrays:
	$(PYTHON) $(TOOLING)/teavm_rom_arrays.py

teavm-mutability:
	$(PYTHON) $(TOOLING)/teavm_mutability.py

array-access-benchmark:
	$(PYTHON) $(TOOLING)/array_access_benchmark.py
