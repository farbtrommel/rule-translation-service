# ANTLR 4

The grammar definitions of the programming languages can you find in this folder.

### Imports

When you use a import statement in a .g4 file make sure that files are in the folder `imports`, 
otherwise the maven antlr4 task can't find the files. 

### Languages

Current languages are:

* DMN FEEL (org.omg.dmn.feel)
* Prova (ws.prova.parser)

### Build

Run the command to build the java class files:
```
$> mvn antlr4:antlr4
```

The generated class files will be in the folder `target/generated-sources/antlr4`.