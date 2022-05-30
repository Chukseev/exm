package anvis;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class AnViS {
	
	public static boolean isRun = true;
	public static Thread [] threadlist = new Thread[0];
	public static long interval = 5000L; //����� ����� ����������� ������ ���������
	public static long lockTime = 30000L; //����� ��������� ���������� ��������� ������
	public static long maxmemoryfree = 1024*1024; //�������� ��������� ����������� ������ JVM � ������, ��� ���������� ��������,
	//�������� ����� ������� ���������� ������ ������������������
	
	public static void main(String[] args) {
		try {
			System.out.println("������� ���������� ������ AnViS ������� ��������!\n��� ��������� ���������� ������� ������� \"info\"");
			Scanner sc = new Scanner(System.in);
			String command = ""; //����������, �������� �������
			while (!command.equals("stop")) { //���� �������� ������� �� �������� �������� stop - ������������ ���� ������ � ��������� ��
				try { //������ ������� ������������ ��������� ������
				command = sc.nextLine(); //���� ������� � ����������
				if (command.equals("stop")) {
					System.out.println("���������� ������ ���������!");
					clear();
					isRun = false;
					Thread.sleep(interval);
				}
				else if (command.equals("list")) {
					optimize();
					list();
				}
				else if (command.equals("lock")) {
					String path = "";
					System.out.print("����������, ������� ���� � ��������� �������: ");
					path = sc.nextLine();
					add(path, false);
				}
				else if (command.equals("lockfull")) {
					String path = "";
					System.out.print("����������, ������� ���� � ��������� �������: ");
					path = sc.nextLine();
					add(path, true);
				}
				else if (command.equals("unlock")) {
					String path = "";
					System.out.print("����������, ������� ���� � ��������� �������: ");
					path = sc.nextLine();
					delete(path);
				}
				else if (command.equals("get time")) {
					System.out.println("����� ��������� � �������������: " + interval);
				}
				else if (command.equals("set time")) {
					System.out.print("����������, ������� ����� ����� ��������� � ������������� (����� �����): ");
					try {
						interval = Long.parseLong(sc.nextLine());
						System.out.println("����� ��������� ������� �����������!");
					}
					catch (Exception e) {
						System.out.println("���� ������� �� �����!");
					}
				}
				else if (command.equals("get locktime")) {
					System.out.println("����� ����������������� ���������� �������� �������� � �������������: " + lockTime);
				}
				else if (command.equals("set locktime")) {
					System.out.print("����������, ������� ����� ����� ����������������� ���������� �������� �������� � ������������� (����� �����): ");
					try {
						lockTime = Long.parseLong(sc.nextLine());
						System.out.println("����� �������� ������� �����������!");
					}
					catch (Exception e) {
						System.out.println("���� ������� �� �����!");
					}
				}
				else if (command.equals("clear")) {
					clear();
				}
				else if (command.equals("info")) {
					System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* ���������� *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\n����� ����������� ���������: Chukseev.nikita@yandex.ru\n������ \"������� ���������� ������ AnViS\"\n�������:\n  info - ������� ���������� � ������ �������\n  stop - ��������� ������ ���������\n  lock - ��������� ����� ���������� ��������� ������� � ��������� ���������\n  lockfull - ��������� ����� ���������� ��������� ������� ��� ��������� ��������\n  unlock - ���������� ����� ���������� ��������� �������\n  list - ����������� ������ ���� �������� �������� ����������� ��� �����������\n  set time - ���������� ����� ���������\n  get time - �������� ����� ���������\n  set locktime - ���������� ����� ����������������� ���������� ��������� �������\n  get locktime - �������� ����� ����������������� ���������� ��������� �������\n  clear - ���������� ��� ������ ���������� �������� ��������\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				}
				else if (command.equals(" ")||command.length()==0) {}
				else {
					System.out.println("������� �� ����������!");
				}
			}
			catch (Exception ee) { //��������� ������
				ee.printStackTrace(); //����� ������
			}
			}
			sc.close();
		}
		catch (Exception e) { //��������� ������
			e.printStackTrace(); //����� ������
		}
	}
	
	public static void add(String path, boolean islockfull) throws Exception { //�������� ����� ���������� ����� �� ���� path
		//islockfull - ����, ������� �������� �� ������ ���������� �����, � �� ���������
		//���� �� ���������� ���� ��������� �����, �� ��������� ��������� ��� ��������� ����� � ��������� ��� ��� ��� �� ����� �������
		if (Runtime.getRuntime().freeMemory()>maxmemoryfree) {
		optimize();
		if (new File(path).exists()) {
		path = new File(path).getAbsolutePath();
		File f = new File(path);
		if (f.isFile()) {
			boolean isFree = true;
			for (int c = 0; c < threadlist.length; c++) {
				if (threadlist[c].getName().equals(path)) {
					isFree = false;
				}
			}
			if (isFree) {
			lock l = new lock(path, islockfull);
			l.start();
			l.setName(path);
			//��������� � ������ ������� ����� �����
			Thread [] ta = new Thread[threadlist.length+1];
			for (int c = 0; c < threadlist.length; c++) {
				ta[c] = threadlist[c];
			}
			ta[threadlist.length] = l;
			threadlist = ta.clone();
			}
			else {
				System.out.println("����� ���������� ����� \"" + path + "\" ��� �������!");
			}
		}
		else {
				int length = f.listFiles().length;
				for (int c = 0; c < length; c++) {
					add(f.listFiles()[c].getAbsolutePath(), islockfull);
				}
		}
		}
		else {
			System.out.println("������ ����� �� ����������!");
		}
		}
		else {
			System.out.println("���������� ������� ������� ������������� ����������� ������ ��� ������ ���������!");
		}
	}
	
	public static void delete(String path) throws Exception { //�������� ������ ���������� ����� �� ���������� ���� path
		//���� �� ���������� ���� ��������� �����, �� ��������� ��������� ��� ��������� ����� � ��������� ��� ��� ��� �� ����� �������
		path = new File(path).getAbsolutePath();
		File f = new File(path);
		if (f.exists()) {
			if (f.isFile()) {
				for (int c = 0; c < threadlist.length; c++) {
					if (threadlist[c]!=null&&threadlist[c].getName().equals(path)) {
						threadlist[c].interrupt();
					}
				}
			}
			else {
				for (int c = 0; c < new File(path).listFiles().length; c++) {
					delete(new File(path).listFiles()[c].getAbsolutePath());
				}
			}
		}
		else {
			System.out.println("������ ����� �� ����������!");
		}
		optimize();
	}
	
	public static void clear () throws Exception { //���������� ��� ������ ���������� ������
		System.out.println("��������� ���� ������� ���������� ������...");
		for (int c = 0; c < threadlist.length; c++) {
			delete(threadlist[c].getName());
		}
		System.out.println("��� ������ ���������� ������ ������� �����������!");
		optimize();
	}
	
	public static void optimize() throws Exception { //������ ������� ������� �� ������� ������� threadlist �� ������,
		//������� �������� �� ��������� ��� �������
		int counter = 0;
		for (int c = 0; c < threadlist.length; c++) {
			if (threadlist[c]==null||!threadlist[c].isAlive()) {
				counter++;
			}
		}
		if (counter > 0) {
			Thread [] temp = new Thread[threadlist.length-counter];
			int s = 0;
			for (int c = 0; c < threadlist.length; c++) {
				if (threadlist[c]!=null&&threadlist[c].isAlive()) {
					temp[s++] = threadlist[c];
				}
			}
			threadlist = temp.clone();
		}
	}
	
	public static void list() throws Exception { //������������ ���� �������� ������� ���������� ������
		if (threadlist.length>0) {
		System.out.print("�������� ������ ���������� ������: ");
		System.out.print("\n[1] " + threadlist[0].getName());
		for (int c = 1; c < threadlist.length; c++) {
			System.out.print(";\n[" + (c+1) + "] " + threadlist[c].getName());
		}
		System.out.println(".");
		}
		else {
			System.out.println("�� ������ ������ �������� ������� ���������� ������ ���!");
		}
	}
	
	public static class lock extends Thread { //����� ���������� ������
		public String path = ""; //���� � �����
		public long time = 0L; //����� ������ ����������
		public boolean isFullLock = false; //���� ������ ����������
		RandomAccessFile raf; //������, ��������� �������� �� � ����� ����������� ����
		
		public lock(String name, boolean islockfull) { //������������� � ������������ ������
			path = name;
			time = System.currentTimeMillis(); //����������� ���������� ���������� ������� ������� �������� �������
			isFullLock = islockfull;
			System.out.println("����� ���������� ����� \"" + path + "\" ������� �������!");
		}
		
		public void run () {
			try {
			if (new File(path).exists()) { //��������� ������ ���� �������� ������ ����������
				File f = new File(path);
				if (f.isFile()) { //��������� ������ ���� �������� ������ �������� �� ������, � ������
					raf = new RandomAccessFile(path, "rw"); //��������� ����
					while ((isRun && (System.currentTimeMillis()-time) < lockTime && !isFullLock) || isFullLock) { //�������, ���� ����������� ������� ��������
						//���� ���������� ���������, �� ���, ���� ������� �����, � ���� ������, �� ��� ������ �������������
						sleep(1L);
					}
					raf.close(); //��������� ������, ����������� ��������� ����
				}
			}
			else {
				System.out.println("��������� ������� �� ���� \"" + path + "\" �� ����������!");
			}
			System.out.println("����� ���������� ����� \"" + path + "\" �������! ��� ����� ���������� ������� ����������!");
			}
			catch (Exception e) { //� ������ ���������� ���� ��������� ������, ����������� ��������� ����
				try {
					raf.close();
				}
				catch (Exception ex) {}
				System.out.println("����� ���������� ����� \"" + path + "\" ����������!");
			}
		}
	}
}