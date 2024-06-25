package pt.ulisboa.tecnico.tuplespaces.server.domain;

import java.util.ArrayList;
import java.util.List;

public class ServerState {

  public class Tuple {
    public String string;
    public Integer clientId;
    public Boolean locked;

    public Tuple(String string) {
      this.string = string;
      this.locked = false;
    }

    public void setClientId(Integer clientId) {
      this.clientId = clientId;
      this.locked = true;
    }
  }

    private List<Tuple> tuples = new ArrayList<>();

    public ServerState() {
    }

    public void put(String tuple) {
      tuples.add(new Tuple(tuple));
    }

    private Tuple getMatchingTuple(String pattern) {
      for (Tuple tuple : tuples) {
        if (tuple.string.matches(pattern)) {
          return tuple;
        }
      }
      return null;
    }

    private List<Tuple> getAllMatchingTuplesStrings(String pattern) {
      List<Tuple> matchingTuples = new ArrayList<>();
      for (Tuple tuple : tuples) {
        if (tuple.string.matches(pattern)) {
          matchingTuples.add(tuple);
        }
      }
      return matchingTuples;
    }

    private void removeMatchingTuple(String pattern) {
      for (Tuple tuple : tuples) {
        if (tuple.string.matches(pattern)) {
          tuples.remove(tuple);
          break;
        }
      }
    }

    public String read(String pattern) {
      Tuple tuple = getMatchingTuple(pattern);
      if (tuple == null) {
        return null;
      }
      return tuple.string;
    }

    public List<String> takePhase1(String pattern, Integer clientId) {
      List<Tuple> matchingTuples = getAllMatchingTuplesStrings(pattern);
      if (matchingTuples.isEmpty()) {
        return null;
      }
      List<String> matchingTuplesStrings = new ArrayList<>();
      for (Tuple tuple : matchingTuples) {
        if (!tuple.locked) {
          tuple.setClientId(clientId);
          matchingTuplesStrings.add(tuple.string);
        }
      }
      return matchingTuplesStrings;
    }

    public void takePhase1Release(Integer clientId) {
      for (Tuple tuple : tuples) {
        if (tuple.locked && tuple.clientId.equals(clientId)) {
          tuple.locked = false;
        }
      }
    }

    public void takePhase2(String tuple, Integer clientId) {
      for (Tuple t : tuples) {
        if (t.string.equals(tuple)) {
          tuples.remove(t);
          break;
        }
      }
      for (Tuple t : tuples)  {
        if (t.locked && t.clientId.equals(clientId)) {
          t.locked = false;
        }
      }
    }

    public List<String> getTupleSpacesState() {
      List<String> tuplesStrings = new ArrayList<>();
      for (Tuple tuple : this.tuples) {
        tuplesStrings.add(tuple.string);
      }
      return tuplesStrings;
    }
}
