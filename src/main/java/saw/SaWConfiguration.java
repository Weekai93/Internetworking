package saw;

import core.*;

public class SaWConfiguration extends Configuration {

    protected int remoteID;

    public SaWConfiguration(int local, Protocol proto) {
        super(proto);
        this.remoteID = local;
    }
}
