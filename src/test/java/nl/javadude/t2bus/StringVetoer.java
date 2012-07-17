package nl.javadude.t2bus;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class StringVetoer {

    private List<String> vetoed = newArrayList();

    @Subscribe(canVeto = true)
    public void veto(String s) throws VetoException {
        vetoed.add(s);
        throw new VetoException(s);
    }

    public List<String> getVetoed() {
        return vetoed;
    }
}
