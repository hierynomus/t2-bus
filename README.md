# T2 Bus
![VW Bus](https://github.com/hierynomus/t2-bus/raw/master/vwbus.jpg)

An enhanced Guava-based event bus, which has the added functionality of veto-ing events.

## Event vetoes
Events that are posted to the event bus can now be vetoed. Once an event is vetoed, it will not be processed further by othoer subscribers to the event.
Any handlers that have canVeto set to true, will be called before any handlers which have canVeto set to false.

A Simple example can look like:

    import nl.javadude.t2bus.EventBus;
    import nl.javadude.t2bus.VetoException;
    import nl.javadude.t2bus.Subscribe;
    
    public class VetoExample {
        public static void main(String[] args) {
            EventBus bus = new EventBus();
            bus.register(new Subscriber());
            bus.register(new Vetoer());
            
            bus.post("Event");
        }
        
        public static class Subscriber {
            @Subscribe
            public void handle(String s) {
                // Will not be called when Vetoer is registered...
            }
        }

        public static class Vetoer {
            @Subscribe(canVeto=true)
            public void veto(String s) throws VetoException {
                throw new VetoException("Veto! [%s]", s);
            }
        }
    }

In this case the Subscriber will never be called for the String event, as the Vetoer will veto each String event coming in.
