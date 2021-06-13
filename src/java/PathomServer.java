import nl.epij.gcf.RingHttpFunction;

public class PathomServer extends RingHttpFunction {
    public String getHandler() {
        return "com.wsscode.pathom-server/app";
    }
}
