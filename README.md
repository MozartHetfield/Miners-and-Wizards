Autor: Soare Ion-Alexandru
Universitatea si Facultatea: UPB, ACS, CTI
Grupa si Seria: 333 CC
Tema a II-a: The miners and the sleepy wizards

Tema are la baza cele 2 structuri de tinere a mesajelor din CommunicationChannel, un ConcurrentHashMap pentru mesajele vrajitorilor,
respectiv un ArrayBlockingQueue pentru cele ale minerilor. Alegerea s-a facut in acest sens deoarece minerii transmit mesaje independente,
in timp ce vrajitorii trebuie sa-si mentina propria ordine, pentru a nu induce in eroare minerii. Asadar, ConcurrentHashMap-ul va avea
cate un ArrayBlockingQueue<Message> pentru fiecare vrajitor care isi asuma un thread (thread perceput ca o variabila de tip long, cu ajutorul
functiei Thread.getCurrentThread().getId()).
	Rata de speedup a implementarii consta in parcurgerea continua a unui array de vrajitori activi, extragand date (mesaje) de la ei cat timp
acestea sunt disponibile. In modul acesta, nu se va irosi niciodata timp (un vrajitor sa nu fie sleepy si sa nu isi faca treaba). Ca modalitate
de sincronizare s-a folosit chiar synchronized asupra listei specifice fiecarui vrajitor in momentul dat. De asemenea, s-a preferat un ArrayBlockingQueue
datorita caracteristicii de FIFO, ce confera mesajelor ordinea dorita. Mai mult, multimea solved din Miner trebuie declarata static pentru a nu se 
actualiza pentru fiecare miner separat (asemeni hashCount-ului).
