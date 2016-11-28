0)
pick a simple `<name>` :)

1)
create `<name>` and `<name>-compiler` modules inside ./module folder
 - `<name>` is for annotations and helper classes <- keep it to minimum
 - `<name>-compiler` is for annotation processor
(for example look at `arg` and `arg-compiler`)

2)
add module to `core-compiler` `eu.f3rog.blade.compiler.BladeProcessor.Module`
(e.g. `ARG("eu.f3rog.blade.compiler.arg.ArgProcessorModule")`)

3)
add dependency to `build.gradle` in `core-compiler`

4)
implement the module logic using unit tests
all tests are in `core-compiler`