package util.replication;

import tp.impl.servers.common.JavaReplicationDirectory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

public class Operation {

    private Foo method;
    private AtomicLong numOperation;
    private Object[] args;

    public Operation(){

    }

    public Operation(Foo method, AtomicLong numOperation, Object[] args){
        super();
        this.method = method;
        this.numOperation = numOperation;
        this.args = args;
    }

    public void execute(JavaReplicationDirectory object){
        this.method.fun(object);
    }

    public AtomicLong getNumOperation(){
        return numOperation;
    }

    public Foo getMethod(){
        return method;
    }

    public Object[] getArgs(){
        return args;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "method=" + method +
                ", numOperation=" + numOperation +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
