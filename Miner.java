import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
	
	private final static int MAGIC = 5;
	static Integer hashCount;
	static Set<Integer> solved;
	CommunicationChannel channel;
	Message message;
	
	/**
	 * Creates a {@code Miner} object.
	 * 
	 * @param hashCount
	 *            number of times that a miner repeats the hash operation when
	 *            solving a puzzle.
	 * @param solved
	 *            set containing the IDs of the solved rooms
	 * @param channel
	 *            communication channel between the miners and the wizards
	 */
	public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
		Miner.hashCount = hashCount;
		Miner.solved = solved;
		this.channel = channel;
		message = new Message(-1, -1, "Hello World!\n");
	}

	private static Boolean hopeIllGraduate(int finalGrade) {
		return (finalGrade >= 5);
	}
	
	private static String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }

        return hashed;
    }

    private static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            // convert to string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
            String hex = Integer.toHexString(0xff & messageDigest[i]);
            if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
    
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
	
	@Override
	public void run() {

		while(hopeIllGraduate(MAGIC)) { 
		
			message = channel.getMessageWizardChannel();
			if (message != null) {
				if (message.getData().equals(Wizard.EXIT)) break;
				if (!message.getData().equals(Wizard.END)) {
					if (!solved.contains(message.getCurrentRoom())) {
						channel.putMessageMinerChannel(new Message(message.getParentRoom(), message.getCurrentRoom(),
								encryptMultipleTimes(message.getData(), hashCount)));
						solved.add(message.getCurrentRoom());
					}
				}
			}	
		}
	}
}
