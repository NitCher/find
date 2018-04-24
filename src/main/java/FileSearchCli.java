import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class FileSearchCli {

    private String[] args = null;

    private HashMap<String, Integer> switchIndexes = new HashMap<String, Integer>();

    private TreeSet<Integer> takenIndexes = new TreeSet<Integer>();

    private boolean isRecursiveSearch;

    private Path searchPath;

    private String[] filesNames;

    private List<File> results = new ArrayList<File>();

 //Конструктор

    public FileSearchCli(String[] args) {
        parse(args);
    }


    public void parse(String[] arguments) {
        args = arguments;

        switchIndexes.clear();

        takenIndexes.clear();

        // Запоминание индексов ключей (аргументов, которые начинаются с символа '-'):
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                switchIndexes.put(args[i], i);
                takenIndexes.add(i);
            }
        }

        isRecursiveSearch = false;

        if (switchPresent("-r")) {
            isRecursiveSearch = true;
        }

        searchPath = FileSystems.getDefault().getPath(".").toAbsolutePath().normalize();

        if (switchPresent("-d")) {
            String directory = switchValue(("-d"));
            searchPath = Paths.get(directory);
        }

        filesNames = this.targets();
    }

    //Проверка наличия среди аргументов заданного ключа с префиксом '-'.
    private boolean switchPresent(String switchName) {
        return switchIndexes.containsKey(switchName);
    }

   //Получения аргумента следующего за ключем (аргументом с префиксом '-').
    private String switchValue(String switchName) {
        if (!switchIndexes.containsKey(switchName))
            return null;

        int switchIndex = switchIndexes.get(switchName);

        if (switchIndex + 1 < args.length) {
            takenIndexes.add(switchIndex + 1);
            return args[switchIndex + 1];
        }

        return null;
    }


    //Возвращение всех аргументов без префикса'-'.
    private String[] targets() {
        String[] targetArray = new String[args.length - takenIndexes.size()];
        int targetIndex = 0;
        for (int i = 0; i < args.length; i++) {
            if (!takenIndexes.contains(i)) {
                targetArray[targetIndex++] = args[i];
            }
        }
        return targetArray;
    }

    //Рекурсивный метод для обхода папок и поиска заданных файлов.
    private void walk(File root) {
        File[] list = root.listFiles();

        if (list == null) {
            return;
        }

        for (File f : list) {
            if (f.isDirectory()) {
                if (isRecursiveSearch) {  // если поиск рекурсивный, то:
                    walk(f);  // заходим во все внутренние папки.
                }
            } else {
                // Сверка имён искомых файлов с именем очередного файла:
                for (String fileName : filesNames) {
                    if (fileName.compareTo(f.getName()) == 0) {
                        this.results.add(f.getAbsoluteFile());
                    }
                }
            }
        }

    }

  //Запуск поиска.
    public List<File> search() {
        results.clear();
        walk(searchPath.toFile());
        return this.results;
    }



    public boolean isRecursiveSearch() {
        return isRecursiveSearch;
    }

    public Path getSearchPath() {
        return searchPath;
    }

    public String[] getFilesNames() {
        return filesNames;
    }

 //Точка входа в программу.
    public static void main(String[] args) {
        FileSearchCli fileSearchCli = new FileSearchCli(args);

        List<File> results = fileSearchCli.search();  // поиск...

        // Вывод результатов поиска:
        for (File file : results) {
            System.out.println(file.toString());
        }
    }
}
