package pt.ulisboa.tecnico.tuplespaces.client.grpc;

import java.util.ArrayList;

public class ResponseCollector {
    ArrayList<String> collectedResponses;

    public ResponseCollector() {
        collectedResponses = new ArrayList<>();
    }

    synchronized public void addString(String s) {
        collectedResponses.add(s);
        notifyAll();
    }

    synchronized public String getString(int n) {
        return collectedResponses.get(n);
    }

    synchronized public String getStrings() {
        String res = new String();
        for (String s : collectedResponses) {
            res = res.concat(s);
        }
        return res;
    }

    synchronized public void waitUntilAllReceived(int n) throws InterruptedException {
        while (collectedResponses.size() < n) {
            wait();
        }
    }
}