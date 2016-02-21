
public class Norme {

	/** Compute || AB ||
	 * 
	 * @param xA
	 * @param yA
	 * @param xB
	 * @param yB
	 * @return
	 */
	public static double norm(int xA, int yA, int xB, int yB ) {
		return Math.sqrt( (xB - xA )*(xB - xA) + (yB - yA)*(yB - yA) );
	}
	
	public static double w(double ri_1, int Ai, double ri, int Bi, double riPlus1, int AiPlus1 ) {
		return (ri_1 * Ai + ri*Bi + riPlus1*AiPlus1 ) / (Ai * AiPlus1);
	}
	public static void print(String s) {
		System.out.println(s);
	}
	
	public static void main(String[] args) {
		double r0 = norm(2, 2, 0, 1);
		double r1 = norm(2, 2, 4, 0);
		double r2 = norm(2, 2, 6, 3);
		double r3 = norm(2, 2, 3, 4);
		double r4 = norm(2, 2, 1, 3);
		double w0 = w(r4, 10, r0, 0, r1, 3); // r4, A0, r0, B0, r1, A4 
		double w1 = w(r0, 10, r1, 2, r2, 6); // r0, A1, r1, B1, r2, A0 
		double w2 = w(r1, 7, r2, 6, r3, 10); // r1, A2, r2, B2, r3, A1 
		double w3 = w(r2, 6, r3, 5, r4, 7); // r2, A3, r3, B3, r4, A2 
		double w4 = w(r3, 3, r4, 3, r0, 6); // r3, A4, r4, B4, r0, A0 
		print("w0 = "+w0);
		print("w1 = "+w1);
		print("w2 = "+w2);
		print("w3 = "+w3);
		print("w4 = "+w4);
		double Swi = w0+w1+w2+w3+w4;
		print("S(wi) = "+Swi);
		
		double l0 = w0 / Swi;
		double l1 = w1 / Swi;
		double l2 = w2 / Swi;
		double l3 = w3 / Swi;
		double l4 = w4 / Swi;
		double Sli = l0 + l1+ l2 + l3 + l4;
		
		print("l0 = "+l0);
		print("l1 = "+l1);
		print("l2 = "+l2);
		print("l3 = "+l3);
		print("l4 = "+l4);
		print("S(li) = "+Sli);
		
		print("Px = "+ (l0*0 + l1*4 + l2*6 * l3*3 + l4*1));
		print("Py = "+ (l0*1 + l1*0 + l2*3 * l3*4 + l4*3));
		
		
	}
}
