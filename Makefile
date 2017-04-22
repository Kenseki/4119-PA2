GS = -g
JC = javac
JVM= java
FILE=
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = gbnnode.java \
	dvnode.java \
	cnnode.java

default: classes

classes: $(CLASSES:.java=.class)

.PHONY: clean
clean:
	rm -f *.class
