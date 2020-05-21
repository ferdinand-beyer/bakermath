all:
	release

watch:
	clj -Adev:watch

release:
	clj -Adev:release

clean:
	rm -rf resources/public/js/
