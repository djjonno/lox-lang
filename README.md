# hyperion
Yet another hobby lang -- An OOP language on the JVM.

I am creating this language to scratch a persistent, lingering itch of mine to demistify the black boxes that is interpreters and compilers. It wouldn't exist without the teachings from craftinginterpreters.com. Thank you @munificent. The core purpose of this lang is to satisfy my curiosities but we could also build a great language along the way.

## What does 'hyperion' even mean?
Don't ask - I have no idea. It just sounds cool. I believe it is a greek god of some sort...

## Roadmap
- object-oriented & functional; functions are first class objects and can be passed around.
- compiled; performance is important and once the grammar has been properly fleshed out to a point where I feel it has stabilised, I will begin porting Hype over to machine by building a compiler with LLVM. LLVM will provide us with good portability cross-platforms and cross-architectures.
- build a garbage collector; I would love to base it off Go-lang's garbage collector so this will involve considerable research into how that works.
- to build the standard library; I plan on developing a fully-fleshed out standard library with functional utils (map, filter, reduce, merge, etc), FileIO, and networking. Writing efficient algorithms here will be fun! Especially since they will be writtin in Hype.
- I am massively inspired by Go-lang go routines - so I would love to at some point implement some kind of channel/pipe mechanism with compile time verification on dead-locks as this is something we can simply verify by inspecting the AST. Also I am quite certain Go-lang also does this so some research will be required here.
- Integration with IDEs - research how the static type checking and code hinting works - can foresee some beautiful traversal algorithms at work here.
- A bare-bones debugger; CLI debugger that supports break points and step-through.
- Package management; I don't like dependency files like poms and package.json. I will research into doing something at the code level. I don't like managing virtualenv's with Python so having independency by default is a good design decision here, I believe. All project packages/dependencies will download into `./build/...` or something.

## The Language
- strongly typed, with compile time type checking
- expressive; I hope to create a language that is expressive by nature without adding too much syntactic sugar like C++ for example. I think they are running out of special chars. Also, once you add the sugar, you cannot take it out. It is there forever.
- core use case; it's just for fun... It doesn't need to have real commercial applications does it. I don't want to bother myself with such details too soon - I simply want to build a beautiful language.

I will provide some snippets of Hype here soon 👍

## Installation

For now you can open the project in your fav IDE (I am using Intellij IDEA which I included the .idea folder so you can use my workspace straight away.) and run Hype.main. You will be presented with an REPL prompt.

Take a look at [script.hype](https://github.com/andjonno/hyperion/blob/master/script.hype) to get a sense of the lang - though early days so don't judge it too harshly.

## Contribute

Coming soon - probably once I write more of the language itself, I may have a clearer path to open contributions appropriately.
