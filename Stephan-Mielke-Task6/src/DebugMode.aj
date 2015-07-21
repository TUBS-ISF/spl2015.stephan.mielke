package Debug;

public aspect DebugMode {
    pointcut debugMethod(): execution(public * *Throwable(..));

    around(): debugMethod() {
        try {
            proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
