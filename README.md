# Hack Assembler

This project is a Hack Assembler written in Java as part of the [NAND to Tetris course](https://www.nand2tetris.org/). It reads Hack assembly language, parses it, and writes compatible 16-bit Hack machine code. It fully supports symbols and labels and ignores all white space and comments.

_Spoiler alert: this is a fully functioning assembler. If you are taking the course, it is recommended that you try writing an assembler from scratch._

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

You need to have Java installed on your machine. You can check if you have Java installed by running the following command in your terminal:

```bash
java -version
```

If you don't have Java installed, you can download it from the [official Oracle website](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).

### Installation

Clone the repository to your local machine:

```bash
git clone https://github.com/brucesdad13/nand2tetris-assembler.git
```

Navigate to the project directory:

```bash
cd nand2tetris-assembler
```

Compile the Java files:

```bash
javac src/*.java
```

## Usage

To use the Hack Assembler, run the `Main` class with the path to your Hack assembly file as an argument:

```bash
java src/Main path_to_input_file.asm path_to_output_file.hack
```

This will generate a `.hack` file containing the translated Hack machine code. Note: the file extensions `.asm` and `.hack` are suggestions.

## Contributing

I want you to know that contributions are welcome. Please open an issue to discuss your ideas before making changes.

## License

This project is licensed under the MIT License. Please take a look at the [LICENSE.md](LICENSE.md) file for details.
