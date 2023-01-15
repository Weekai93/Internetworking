package saw;

import core.*;

public class SaWConfiguration extends Configuration {
    public int getRemoteID() {
        return remoteID;
    }

    public void setRemoteID(int remoteID) {
        this.remoteID = remoteID;
    }

    protected int remoteID;

    public SaWConfiguration(int local, Protocol proto) {
        super(proto);
        this.remoteID = local;
    }
}
