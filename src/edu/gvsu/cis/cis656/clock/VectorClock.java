package edu.gvsu.cis.cis656.clock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class VectorClock implements Clock {

    // suggested data structure ...
    private Map<String, Integer> clock = new Hashtable<String, Integer>();

    @Override
    public void update(Clock other) {
//update the clock with a new one.  Keep old values but update differences.


        for (Map.Entry<String, Integer> entry : clock.entrySet()) {
            //gets the process ID (String)
            String processID = entry.getKey();
            //converts to an integer for use with getTime
            Integer IdNum = Integer.parseInt(processID);
            //gets time for clock 1
            int time = entry.getValue();
            //gets time for clock 2
            Integer otherTime = other.getTime(IdNum);

            clock.put(entry.getKey(), Math.max(entry.getValue(), otherTime));
        }

        JSONObject otherClockasJSON = new JSONObject(other.toString());
        Map<String, Object> otherClockasMap = otherClockasJSON.toMap();
        for (Map.Entry<String, Object> entry : otherClockasMap.entrySet()) {
            if (!clock.containsKey(entry.getKey())) {
                clock.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
            }
        }
    }

    @Override
    public void setClock(Clock other) {

        for (Map.Entry<String, Integer> entry : clock.entrySet()) {

            String processID = entry.getKey();
            Integer IdNum = Integer.parseInt(processID);
            int time = entry.getValue();
            int otherTime = other.getTime(IdNum);
            entry.setValue(otherTime);

        }
    }

    @Override
    public void tick(Integer pid) {
        String p = Integer.toString(pid);

        if (clock.containsKey(p)) {
            clock.replace(p, clock.get(p) + 1);
        }
    }

    @Override
    public boolean happenedBefore(Clock other) {

        JSONObject clockAsJSON = new JSONObject(other.toString());
        Map<String, Object> newJSONclock = clockAsJSON.toMap();

        for (Map.Entry<String, Object> entry : newJSONclock.entrySet()) {
            if (clock.containsKey(entry.getKey())) {
                if (Integer.parseInt(entry.getValue().toString()) < clock.get(entry.getKey())) {
                    return false;
                }
            }
        }

        return true;
    }


    public String toString() {
        // TODO: you implement
        JSONObject output = new JSONObject(clock);
        return output.toString();

    }

    @Override
    public void setClockFromString(String clock) {
        JSONObject clockAsJSON = new JSONObject(clock);
        Map<String, Object> newJSONclock = clockAsJSON.toMap();
        Map<String, Integer> newClock = new HashMap<>();

        try {
            for (Map.Entry<String, Object> entry : newJSONclock.entrySet()) {
                newClock.put(entry.getKey(), Integer.parseInt(entry.getValue().toString()));
            }
            this.clock = newClock;
        } catch (NumberFormatException e) {
            //bails if the number format is incorrect.
        }

    }

    @Override
    public int getTime(int p) {
        String pid = Integer.toString(p);
        if (clock.containsKey(pid)) {
            int time = this.clock.get(pid);
            return time;

        }
        return 0;
    }

    @Override
    public void addProcess(int p, int c) {

        Integer pid = p;
        clock.put(pid.toString(), c);
    }
}
