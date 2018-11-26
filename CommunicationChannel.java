import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
	ArrayBlockingQueue <Message> allMinersMessages;
	ConcurrentHashMap<Long, ArrayBlockingQueue<Message>> allWizardsMessages;
	//ReentrantLock locker;
	
	/**
	 * Creates a {@code CommunicationChannel} object.
	 */
	public CommunicationChannel() {
		allMinersMessages = new ArrayBlockingQueue <Message> (25000);
		allWizardsMessages = new ConcurrentHashMap<Long, ArrayBlockingQueue<Message>>();
		//locker = new ReentrantLock();
	}

	public static Message combineMessages(Message parent, Message node) {
		return new Message(parent.getCurrentRoom(), node.getCurrentRoom(), node.getData());
	}
	
	/**
	 * Puts a message on the miner channel (i.e., where miners write to and wizards
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */
	public void putMessageMinerChannel(Message message) {
		try {
			allMinersMessages.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a message from the miner channel (i.e., where miners write to and
	 * wizards read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageMinerChannel() {
		try {
			return allMinersMessages.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Puts a message on the wizard channel (i.e., where wizards write to and miners
	 * read from).
	 * 
	 * @param message
	 *            message to be put on the channel
	 */	
	public void putMessageWizardChannel(Message message) {
		
		long currentThreadId = Thread.currentThread().getId();
		ArrayBlockingQueue<Message> helper = new ArrayBlockingQueue<Message>(25000);
		
		if (message.getData().equals(Wizard.END)) return;
		
		try {
			helper.put(message);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (allWizardsMessages.putIfAbsent(currentThreadId, helper) != null) {
			helper =  new ArrayBlockingQueue <Message> (25000);
			helper = allWizardsMessages.get(currentThreadId);
			
			try {
				helper.put(message);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			allWizardsMessages.put(currentThreadId, helper);
		}
	}

	/**
	 * Gets a message from the wizard channel (i.e., where wizards write to and
	 * miners read from).
	 * 
	 * @return message from the miner channel
	 */
	public Message getMessageWizardChannel() {

		ArrayList<Long> availableWizards = new ArrayList<Long> (allWizardsMessages.keySet());
        Message messageHelperOne = new Message(-1, -1, "HAVE: Apartment in the middle of Bucharest");
        Message messageHelperTwo = new Message(-1, -1, "WANT: Metallica ticket (I pay the difference)");

        for (int i = 0; i < availableWizards.size(); i++) {
        	
        	long currentThreadId = availableWizards.get(i);
			ArrayBlockingQueue<Message> currentWizardMessages = allWizardsMessages.get(currentThreadId);
		
			
			synchronized (allWizardsMessages.get(currentThreadId)) {
				if (currentWizardMessages.isEmpty()) continue;
				if (currentWizardMessages.size() == 1){
					if (currentWizardMessages.peek().getData().equals(Wizard.EXIT)) {
							// || currentWizardMessages.peek().getData().equals(Wizard.END)) {
						//locker.lock();
						try {
							messageHelperOne = currentWizardMessages.take();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						//locker.unlock();
						return messageHelperOne;
					}
				} else {
					//locker.lock();
					try {
						messageHelperOne = currentWizardMessages.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					try {
						messageHelperTwo = currentWizardMessages.take();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//locker.unlock();
					return combineMessages(messageHelperOne, messageHelperTwo);
				}
			}
		}
		return new Message(-1, -1, messageHelperOne.getData().concat(messageHelperTwo.getData()));
	}
}
