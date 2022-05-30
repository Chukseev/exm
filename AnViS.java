package anvis;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class AnViS {
	
	public static boolean isRun = true;
	public static Thread [] threadlist = new Thread[0];
	public static long interval = 5000L; //время перед завершением работы программы
	public static long lockTime = 30000L; //время временной блокировки указанных файлов
	public static long maxmemoryfree = 1024*1024; //значение свободной оперативной памяти JVM в байтах, при достижении которого,
	//создание новых потоков блокировки файлов приостанавливается
	
	public static void main(String[] args) {
		try {
			System.out.println("Система блокировки файлов AnViS успешно запущена!\nДля получения инструкции введите команду \"info\"");
			Scanner sc = new Scanner(System.in);
			String command = ""; //переменная, хранящая команду
			while (!command.equals("stop")) { //пока введённая команда не является командой stop - обрабатывать ввод команд и выполнять их
				try { //начало области отслеживания обработки ошибок
				command = sc.nextLine(); //ввод команды с клавиатуры
				if (command.equals("stop")) {
					System.out.println("Завершение работы программы!");
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
					System.out.print("Пожалуйста, введите путь к файловому объекту: ");
					path = sc.nextLine();
					add(path, false);
				}
				else if (command.equals("lockfull")) {
					String path = "";
					System.out.print("Пожалуйста, введите путь к файловому объекту: ");
					path = sc.nextLine();
					add(path, true);
				}
				else if (command.equals("unlock")) {
					String path = "";
					System.out.print("Пожалуйста, введите путь к файловому объекту: ");
					path = sc.nextLine();
					delete(path);
				}
				else if (command.equals("get time")) {
					System.out.println("Время интервала в миллисекундах: " + interval);
				}
				else if (command.equals("set time")) {
					System.out.print("Пожалуйста, введите новое время интервала в миллисекундах (целое число): ");
					try {
						interval = Long.parseLong(sc.nextLine());
						System.out.println("Время интервала успешно установлено!");
					}
					catch (Exception e) {
						System.out.println("Было введено не число!");
					}
				}
				else if (command.equals("get locktime")) {
					System.out.println("Время продолжительности блокировки файловых объектов в миллисекундах: " + lockTime);
				}
				else if (command.equals("set locktime")) {
					System.out.print("Пожалуйста, введите новое время продолжительности блокировки файловых объектов в миллисекундах (целое число): ");
					try {
						lockTime = Long.parseLong(sc.nextLine());
						System.out.println("Время задержки успешно установлено!");
					}
					catch (Exception e) {
						System.out.println("Было введено не число!");
					}
				}
				else if (command.equals("clear")) {
					clear();
				}
				else if (command.equals("info")) {
					System.out.println("*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-* Информация *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*\nАдрес технической поддержки: Chukseev.nikita@yandex.ru\nПроект \"Система блокировки файлов AnViS\"\nКоманды:\n  info - вывести информацию о данном проекте\n  stop - завершить работу программы\n  lock - запустить поток блокировки файлового объекта с временной привязкой\n  lockfull - запустить поток блокировки файлового объекта без временной привязки\n  unlock - остановить поток блокировки файлового объекта\n  list - отображение списка всех файловых объектов находящихся под блокировкой\n  set time - установить время интервала\n  get time - получить время интервала\n  set locktime - установить время продолжительности блокировки файлового объекта\n  get locktime - получить время продолжительности блокировки файлового объекта\n  clear - остановить все потоки блокировки файловых объектов\n*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				}
				else if (command.equals(" ")||command.length()==0) {}
				else {
					System.out.println("Команда не распознана!");
				}
			}
			catch (Exception ee) { //обработка ошибок
				ee.printStackTrace(); //вывод ошибок
			}
			}
			sc.close();
		}
		catch (Exception e) { //обработка ошибок
			e.printStackTrace(); //вывод ошибок
		}
	}
	
	public static void add(String path, boolean islockfull) throws Exception { //добавить поток блокировки файла по пути path
		//islockfull - флаг, который отвечает за полную блокировку файла, а не временную
		//если по указанному пути находится папка, то сканируем абсолютно все вложенные файлы и выполняем для них эту же самую функцию
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
			//добавляем в массив потоков новый поток
			Thread [] ta = new Thread[threadlist.length+1];
			for (int c = 0; c < threadlist.length; c++) {
				ta[c] = threadlist[c];
			}
			ta[threadlist.length] = l;
			threadlist = ta.clone();
			}
			else {
				System.out.println("Поток блокировки файла \"" + path + "\" уже запущен!");
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
			System.out.println("Такого файла не существует!");
		}
		}
		else {
			System.out.println("Достигнута верхняя граница использования оперативной памяти для данной программы!");
		}
	}
	
	public static void delete(String path) throws Exception { //удаление потока блокировки файла по указанному пути path
		//если по указанному пути находится папка, то сканируем абсолютно все вложенные файлы и выполняем для них эту же самую функцию
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
			System.out.println("Такого файла не существует!");
		}
		optimize();
	}
	
	public static void clear () throws Exception { //остановить все потоки блокировки файлов
		System.out.println("Остановка всех потоков блокировки файлов...");
		for (int c = 0; c < threadlist.length; c++) {
			delete(threadlist[c].getName());
		}
		System.out.println("Все потоки блокировки файлов успешно остановлены!");
		optimize();
	}
	
	public static void optimize() throws Exception { //данная функция удаляет из массива потоков threadlist те потоки,
		//которые являются не активными или пустыми
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
	
	public static void list() throws Exception { //перечисление всех активных потоков блокировки файлов
		if (threadlist.length>0) {
		System.out.print("Активные потоки блокировки файлов: ");
		System.out.print("\n[1] " + threadlist[0].getName());
		for (int c = 1; c < threadlist.length; c++) {
			System.out.print(";\n[" + (c+1) + "] " + threadlist[c].getName());
		}
		System.out.println(".");
		}
		else {
			System.out.println("На данный момент активных потоков блокировки файлов нет!");
		}
	}
	
	public static class lock extends Thread { //поток блокировки файлов
		public String path = ""; //путь к файлу
		public long time = 0L; //время начала блокировки
		public boolean isFullLock = false; //флаг полной блокировки
		RandomAccessFile raf; //объект, благодаря которому мы и будем блокировать файл
		
		public lock(String name, boolean islockfull) { //инициализация в конструкторе класса
			path = name;
			time = System.currentTimeMillis(); //присваеваем переменной начального времени текущее значение времени
			isFullLock = islockfull;
			System.out.println("Поток блокировки файла \"" + path + "\" успешно запущен!");
		}
		
		public void run () {
			try {
			if (new File(path).exists()) { //выполняем только если файловый объект существует
				File f = new File(path);
				if (f.isFile()) { //выполняем только если файловый объект является не папкой, а файлом
					raf = new RandomAccessFile(path, "rw"); //блокируем файл
					while ((isRun && (System.currentTimeMillis()-time) < lockTime && !isFullLock) || isFullLock) { //ожидаем, пока выполняется условие ожидания
						//если блокировка временная, то ждём, пока истечёт время, а если полная, то ждём ручной разблокировки
						sleep(1L);
					}
					raf.close(); //закрываем объект, блокирующий указанный файл
				}
			}
			else {
				System.out.println("Файлового объекта по пути \"" + path + "\" не существует!");
			}
			System.out.println("Время блокировки файла \"" + path + "\" истекло! Его поток блокировки успешно остановлен!");
			}
			catch (Exception e) { //в случае исключения тоже закрываем объект, блокирующий указанный файл
				try {
					raf.close();
				}
				catch (Exception ex) {}
				System.out.println("Поток блокировки файла \"" + path + "\" остановлен!");
			}
		}
	}
}