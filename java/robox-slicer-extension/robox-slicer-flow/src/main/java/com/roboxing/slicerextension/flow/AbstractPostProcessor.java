package com.roboxing.slicerextension.flow;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.roboxing.slicerextension.flow.annotations.Pattern;
import com.roboxing.slicerextension.flow.annotations.Patterns;
import com.roboxing.slicerextension.flow.utils.LineProcessor;
import com.roboxing.slicerextension.flow.utils.LineProcessor.VarStringMethod;

public abstract class AbstractPostProcessor {

    protected LineProcessor lineProcessor = new LineProcessor();
    protected RandomAccessFile output;

    public AbstractPostProcessor() {
        collectAnnotations();
    }

    private void collectAnnotations() {
        Class<? extends AbstractPostProcessor> cls = getClass();

        for (final Method method : cls.getDeclaredMethods()) {
            Patterns patterns = method.getAnnotation(Patterns.class);
            if (patterns != null) {
                final Class<?>[] parameterTypes = method.getParameterTypes();
                VarStringMethod m = (String... args) -> {
                    if (args.length < parameterTypes.length) {
                        throw new IndexOutOfBoundsException("No enough groups matched for the method " + method.getName());
                    }

                    Object[] methodArgs = new Object[parameterTypes.length];
                    for (int i = 0; i < methodArgs.length; i++) {
                        methodArgs[i] = convert(args[i], parameterTypes[i]);
                    }

                    try {
                        method.invoke(this, methodArgs);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                for (Pattern pattern : patterns.value()) {
                    String patternString = pattern.value();
                    lineProcessor.add(patternString, m);
                }
            }
        }
    }

    public void processFile(File inFile, File outFile) throws IOException {
        try (Scanner scanner = new Scanner(inFile, "UTF-8");
                RandomAccessFile output = new RandomAccessFile(outFile, "rw")) {
            processFile(scanner, output);
        }
    }

    public void processFile(Scanner scanner, RandomAccessFile output) throws IOException {
        this.output = output;
        output.setLength(0);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            boolean result = lineProcessor.processLine(line);
            if (!result) {
                handleUnprocessedLine(line);
            }
            afterLine(line);
        }
    }

    public boolean processLine(String line) {
        return lineProcessor.processLine(line);
    }

    // Factory method
    protected void afterLine(String line) {

    }

    // Overwrite if needed!
    protected void handleUnprocessedLine(String line) throws IOException {
        writeOutput(line);
    }

    protected void writeOutput(String textToWrite) throws IOException {
        output.write(StandardCharsets.UTF_8.encode(textToWrite).array());
    }

    public Object convert(Object v, Class<?> resClass) {
        if (v.getClass().isAssignableFrom(resClass)) {
            return v;
        }

        if (v instanceof String) {
            String s = (String) v;

            String n = resClass.getName();
            if (n.equals("boolean")) {
                return new Boolean(s);
            } else if (n.equals("java.lang.Boolean")) {
                return new Boolean(s);
            } else if (n.equals("byte")) {
                return new Byte(s);
            } else if (n.equals("java.lang.Byte")) {
                return new Byte(s);
            } else if (n.equals("char")) {
                return s.charAt(0);
            } else if (n.equals("java.lang.Character")) {
                return s.charAt(0);
            } else if (n.equals("double")) {
                return new Double(s);
            } else if (n.equals("java.lang.Double")) {
                return new Double(s);
            } else if (n.equals("float")) {
                return new Float(s);
            } else if (n.equals("java.lang.Float")) {
                return new Float(s);
            } else if (n.equals("int")) {
                return new Integer(s);
            } else if (n.equals("java.lang.Integer")) {
                return new Integer(s);
            } else if (n.equals("long")) {
                return new Long(s);
            } else if (n.equals("java.lang.Long")) {
                return new Long(s);
            } else if (n.equals("short")) {
                return new Short(s);
            } else if (n.equals("java.lang.Short")) {
                return new Short(s);
            } else if (n.equals("java.math.BigInteger")) {
                return new BigInteger(s);
            } else if (n.equals("java.math.BigDecimal")) {
                return new BigDecimal(s);
            } else if (n.equals("java.lang.Object")) {
                return s;
            } else {
                throw new RuntimeException("Cannot convert '" + s + "'(" + s.getClass().getName() + ") to " + resClass.getName());
            }
        } else {
            throw new RuntimeException("Cannot convert '" + v.toString() + "'(" + v.getClass().getName() + ") to " + resClass.getName());
        }
    }

}
