import java.io.*;
import java.util.*;

/**
 * класс с реалезацией всех команд
 */
public abstract class CollectionManager {

    /**
     *реализация команды help
     */
    public static void help(MyCollection o, Scanner sc, String[] arg) {
        if(checksForExtraArguments(arg)) return;
        try(FileReader reader = new FileReader(System.getProperty("user.dir")+"\\src\\HelpAboutCommand.txt"))
        {
            int c;
            while((c=reader.read())!=-1){
                System.out.print((char)c);
            }
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     *реализация команды info
     */
    public static void info(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        System.out.println("Тип коллекции: " + o.getTypeCollection().getName());
        System.out.println("Дата инициализации коллекции: " + o.getCollectionCreationDate());
        System.out.println("Количество элементов коллекции: " + o.size());
    }

    /**
     *реализация команды show
     */
    public static void show(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        if (!o.isEmpty()){
            for (Object i : o){
            o.display((Flat) i);
            }
        } else System.out.println("Коллекция не содержит элементов");
    }

    /**
     *реализация команды add {element}
     */
    public static void add(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        aadWithOutput(o, FlatReader.readFlat(sc, o));
    }

    /**
     *реализация команды update id {element}
     */
    public static void update(MyCollection o, Scanner sc, String[] arg){
        if (arg.length < 1) {
            System.out.println("Нужен id");
            return;
        }
        long id;
        try {
            id = Long.parseLong(arg[0]);
        } catch (NumberFormatException e) {
            System.out.println("id должен быть натуральным числом");
            return;
        }
        Flat flat = null;
        for (Object i : o){
            if(((Flat)i).getId() == id) flat = (Flat) i;
        }
        if(flat != null){
            Flat f = FlatReader.readFlat(sc, null);
            flat.setName(f.getName());
            flat.setCoordinates(f.getCoordinates());
            flat.setArea(f.getArea());
            flat.setNumberOfRooms(f.getNumberOfRooms());
            flat.setFurnish(f.getFurnish());
            flat.setView(f.getView());
            flat.setTransport(f.getTransport());
            flat.setHouse(f.getHouse());
            System.out.println("\nЭлемент обновлен\n");
        } else System.out.println("Элемент с таким id не найден");
    }

    /**
     *реализация команды remove_by_id id
     */
    public static void removeById(MyCollection o, Scanner sc, String[] arg){
        if (arg.length < 1) {
            System.out.println("Нужен id");
            return;
        }
        long id;
        try {
            id = Long.parseLong(arg[0]);
        } catch (NumberFormatException e) {
            System.out.println("id должен быть натуральным числом");
            return;
        }
        Flat flat = null;
        for (Object i : o){
            if(((Flat)i).getId() == id) flat = (Flat) i;
        }
        if(flat != null){
            removeWithOutput(o, flat);
        } else System.out.println("Элемент с таким id не найден");
    }

    /**
     *реализация команды clear
     */
    public static void clear(MyCollection o, Scanner sc, String[] arg) {
        if(checksForExtraArguments(arg)) return;
        o.clear();
    }

    /**
     *реализация команды save
     */
    public static void save(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        if (Main.getWorkFile() == null){
            System.out.println("Не задан файл для сохранения\n");
            return;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(Main.getWorkFile());
            byte[] buffer = ParserJson.parseFromCollectionToFromJsonString(o).getBytes();
            fileOutputStream.write(buffer, 0, buffer.length);
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
            return;
        }
        System.out.println("Сохранение завершено\n");
    }

    /**
     *реализация команды execute_script file_name
     */
    public static void executeScript(MyCollection o, Scanner sc, String[] arg){
        if (arg.length < 1) {
            System.out.println("Нужно полное имя файла");
            return;
        }
        String fileName = arg[0];
        FileInputStream f;
        try {
            File file = new File(fileName);
            f = new FileInputStream(file);
        } catch (FileNotFoundException e){
            System.out.println("Файл не найден или не хватает прав для его чтения");
            return;
        }
        Scanner scanner = new Scanner(f);
        BankCommand bankCommand = new BankCommand();
        try {
            while (scanner.hasNextLine()){
                String[] arguments = scanner.nextLine().trim().split(" ");
                if (arguments.length == 0) continue;
                if (arguments[0].equals("")) continue;
                String command = arguments[0];
                arguments = Arrays.copyOfRange(arguments, 1, arguments.length);

                if (command.equals("execute_script") && arguments[0].equals(fileName)){
                    System.out.println("\nВаш скипт вызывает сам себя, он никогда не закончит выполнение\n");
                    return;
                }

                if (bankCommand.commandMap.get(command) != null){
                    bankCommand.commandMap.get(command).accept(o, scanner, arguments);
                } else {
                    System.out.println("\nОшибка в содержании файла: " + command);
                    return;
                }
            }
        } catch (NoSuchElementException e){
            System.out.println("\nОшибка в содержании файла");
            return;
        }
        System.out.println("\nСкрипт выполнен\n");
    }

    /**
     *реализация команды exit
     */
    public static void exit(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        System.exit(0);
    }

    /**
     *реализация команды add_if_min {element}
     */
    public static void addIfMin(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        Flat flat = FlatReader.readFlat(sc, o);
        try {
            if (((Flat)o.first()).compareTo(flat) > 0) {
                aadWithOutput(o, flat);
            } else System.out.println("Значение этого элемента больше, чем у наименьшего элемента коллекции");
        } catch (NoSuchElementException e){
            aadWithOutput(o, flat);
        }
    }

    /**
     *реализация команды remove_greater {element}
     */
    public static void removeGreater(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        Flat f = FlatReader.readFlat(sc, null);
        TreeSet<Flat> buffer = new TreeSet<>();
        for (Object i : o) if (((Flat)i).compareTo(f) > 0) buffer.add((Flat) i);
        if(!buffer.isEmpty()){
            buffer.forEach(o::remove);
        } else System.out.println("\nЭлементов превышающие заданный не найдено\n");
    }

    /**
     *реализация команды remove_lower {element}
     */
    public static void removeLower(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        Flat f = FlatReader.readFlat(sc, null);
        TreeSet<Flat> buffer = new TreeSet<>();
        for (Object i : o) if (((Flat)i).compareTo(f) < 0) buffer.add((Flat) i);
        if(!buffer.isEmpty()){
            buffer.forEach(o::remove);
        } else System.out.println("\nЭлементов меньшие, чем заданный не найдено\n");
    }

    /**
     *реализация команды group_counting_by_creation_date
     */
    public static void groupCountingByCreationDate(MyCollection o, Scanner sc, String[] arg){
        if(checksForExtraArguments(arg)) return;
        if (o.isEmpty()){
            System.out.println("Коллекция не содеждит элементов");
            return;
        }
        HashMap<java.time.LocalDate, MyCollection> groupMap = new HashMap<>();
        for (Object i : o){
            if (groupMap.get(((Flat)i).getCreationDate()) == null){
                MyCollection x = new MyCollection();
                x.add(i);
                groupMap.put(((Flat)i).getCreationDate(), x);
            } else groupMap.get(((Flat) i).getCreationDate()).add(i);
        }
        for (Map.Entry<java.time.LocalDate, MyCollection> entry : groupMap.entrySet()){
            System.out.println("    Элементы созданные " + entry.getKey() + " :\n");
            show(entry.getValue(), sc, arg);
        }
    }

    /**
     *реализация команды filter_contains_name name
     */
    public static void filterContainsName(MyCollection o, Scanner sc, String[] arg){
        if (!o.isEmpty()){
            if (arg.length < 1) {
                System.out.println("Нужна подстрока");
                return;
            }
            String name = arg[0];
            int counter = 0;
            for (Object i : o){
               Flat flat = (Flat) i;
               if (flat.getName().contains(name)){
                   o.display(flat);
                   counter++;
               }
            }
            System.out.printf("Найдено %d элементов, название которых содержит '%s' \n", counter, name);
        } else System.out.println("Коллекция не содержит элементов");
    }

    /**
     *реализация команды print_field_ascending_number_of_rooms numberOfRooms
     */
    public static void printFieldAscendingNumberOfRooms(MyCollection o, Scanner sc, String[] arg){
        if (arg.length < 1) {
            System.out.println("Нужно значение numberOfRooms");
            return;
        }
        long numberOfRooms;
        try {
            numberOfRooms = Long.parseLong(arg[0]);
        } catch (NumberFormatException e) {
            System.out.println("Значение numberOfRooms должнo быть натуральным числом");
            return;
        }
        ArrayList<Long> arrayListNumbersOfRooms = new ArrayList<>();
        for (Object i : o){
            if(((Flat)i).getNumberOfRooms() >= numberOfRooms) arrayListNumbersOfRooms.add(((Flat)i).getNumberOfRooms());
        }
        Collections.sort(arrayListNumbersOfRooms);
        arrayListNumbersOfRooms.forEach(System.out::println);
    }

    private static boolean checksForExtraArguments(String[] arg){
        if (arg.length != 0){
            System.out.println("Для этой команды не нужны аргументы");
            return true;
        }
        return false;
    }

    private static void aadWithOutput(MyCollection o, Flat flat){
        boolean e = o.add(flat);
        if (e){
            System.out.println("\nЭлемент успешно добавлен\n");
        } else {
            System.out.println("\nТакой элемент уже есть\n");
        }
    }

    private static void removeWithOutput(MyCollection o, Flat flat){
        boolean e = o.remove(flat);
        if (e){
            System.out.println("\nЭлемент успешно удален\n");
        } else {
            System.out.println("\nЭлемент не найден\n");
        }
    }
}
