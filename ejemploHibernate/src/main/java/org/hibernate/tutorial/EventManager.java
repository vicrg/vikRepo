package org.hibernate.tutorial;

import org.hibernate.Session;

import java.util.*;

import org.hibernate.tutorial.domain.Event;
import org.hibernate.tutorial.domain.Person;
import org.hibernate.tutorial.util.HibernateUtil;
@SuppressWarnings("unused")
public class EventManager {

	public static void main(String[] args) {
        EventManager mgr = new EventManager();

        if (args[0].equals("store")) {
            mgr.createAndStoreEvent("My Event", new Date());
        }
        else if (args[0].equals("list")) {
            List<?> events = mgr.listEvents();
            for (int i = 0; i < events.size(); i++) {
                Event theEvent = (Event) events.get(i);
                System.out.println(
                        "Event: " + theEvent.getTitle() + " Time: " + theEvent.getDate()
                );
            }
        }
        else if (args[0].equals("addpersontoevent")) {
            Long eventId = mgr.createAndStoreEvent("My Event", new Date());
            Long personId = mgr.createAndStorePerson("Foo", "Bar");
            mgr.addPersonToEvent(personId, eventId);
            System.out.println("Added person " + personId + " to event " + eventId);
        }
        HibernateUtil.getSessionFactory().close();
    }
	
	 private List<?> listEvents() {
	        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	        session.beginTransaction();
	        List<?> result = session.createQuery("from Event").list();
	        session.getTransaction().commit();
	        return result;
	    }
	 
	 private Long createAndStoreEvent(String title, Date theDate) { 
		 Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		 session.beginTransaction();
		 
		 Event theEvent = new Event();
		 theEvent.setTitle(title);
		 theEvent.setDate(theDate); 
		 session.save(theEvent); 
		 Long eventId = theEvent.getId();
		 session.getTransaction().commit();
		 
		 return eventId; 
	
	 }
	 
	 //createAndStoreEvent sin valor de retorno
//    private void createAndStoreEvent(String title, Date theDate) {
//        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
//        session.beginTransaction();
//
//        Event theEvent = new Event();
//        theEvent.setTitle(title);
//        theEvent.setDate(theDate);
//        session.save(theEvent);
//
//        session.getTransaction().commit();
//    }
	 
	 private Long createAndStorePerson(String firstName, String lastName) { 
		 Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		 session.beginTransaction(); 
		 
		 Person thePerson = new Person(); 
		 thePerson.setFirstname(firstName); 
		 thePerson.setLastname(lastName);
		 session.save(thePerson);
		 Long personId = thePerson.getId();
		 session.getTransaction().commit();
		 
		 return personId; 
	}
	 
	 
	@SuppressWarnings("unchecked")
	private void addEmailToPerson(Long personId, String emailAddress) {
	        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
	        session.beginTransaction();

	        Person aPerson = (Person) session.load(Person.class, personId);
	        // adding to the emailAddress collection might trigger a lazy load of the collection
	        aPerson.getEmailAddresses().add(emailAddress);

	        session.getTransaction().commit();
	 } 
    
    @SuppressWarnings("unchecked")
	private void addPersonToEvent(Long personId, Long eventId) {
        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        session.beginTransaction();

        Person aPerson = (Person) session
                .createQuery("select p from Person p left join fetch p.events where p.id = :pid")
                .setParameter("pid", personId)
                .uniqueResult(); // Eager fetch the collection so we can use it detached
        Event anEvent = (Event) session.load(Event.class, eventId);

        session.getTransaction().commit();

        // End of first unit of work

        aPerson.getEvents().add(anEvent); // aPerson (and its collection) is detached

        // Begin second unit of work

        Session session2 = HibernateUtil.getSessionFactory().getCurrentSession();
        session2.beginTransaction();
        session2.update(aPerson); // Reattachment of aPerson

        session2.getTransaction().commit();
    }
	
}
