SHADOW_CLJS := npx shadow-cljs

all: compile

compile:
	$(SHADOW_CLJS) compile main

test:
	$(SHADOW_CLJS) compile test

watch:
	$(SHADOW_CLJS) watch main test

release:
	$(SHADOW_CLJS) release

start:
	$(SHADOW_CLJS) start

stop:
	$(SHADOW_CLJS) stop

clean:
	rm -rf out/ resources/public/js/
