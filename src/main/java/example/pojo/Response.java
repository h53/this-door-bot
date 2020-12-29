package example.pojo;

import java.util.Arrays;

public class Response {
    private int id;
    private EventBean []event;

    public EventBean[] getEvent() {
        return event;
    }

    public void setEvent(EventBean[] event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Response{" +
                "id=" + id +
                ", event=" + Arrays.toString(event) +
                '}';
    }
}

