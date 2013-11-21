package net.codjo.maven.enforcer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProcessor {
    private String text;


    public StringProcessor(String text) {
        this.text = text;
    }


    public Finder find(String regex) {
        return new SimpleFinder(text).find(regex);
    }


    public interface Finder {
        Finder find(String regex);


        Finder containing(String regex);


        Finder notContaining(String regex);


        boolean hasFound();
    }

    class SimpleFinder implements Finder {
        private final String text;
        private boolean found = true;


        SimpleFinder(String text) {
            this.text = text;
        }


        public Finder find(String regex) {
            if (found) {
                List result = findAll(regex);
                if (result.isEmpty()) {
                    found = false;
                    return this;
                }
                List finders = new ArrayList();
                for (Iterator it = result.iterator(); it.hasNext();) {
                    finders.add(new SimpleFinder((String)it.next()));
                }
                return new CompositeFinder(finders);
            }
            else {
                return this;
            }
        }


        public Finder containing(String regex) {
            found = found && contains(regex);
            return this;
        }


        public Finder notContaining(String regex) {
            found = found && !contains(regex);
            return this;
        }


        public boolean hasFound() {
            return found;
        }


        private List findAll(String regex) {
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            List result = new ArrayList();
            while (matcher.find()) {
                result.add(matcher.group());
            }
            return result;
        }


        private boolean contains(String regex) {
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(text);
            return matcher.find();
        }
    }

    class CompositeFinder implements Finder {
        private List finders;


        CompositeFinder(List finders) {
            this.finders = finders;
        }


        public Finder find(String regex) {
            List newFinders = new ArrayList();
            for (Iterator it = finders.iterator(); it.hasNext();) {
                Finder finder = (Finder)it.next();
                newFinders.add(finder.find(regex));
            }
            this.finders = newFinders;
            return this;
        }


        public Finder containing(String regex) {
            for (Iterator it = finders.iterator(); it.hasNext();) {
                Finder finder = (Finder)it.next();
                finder.containing(regex);
            }
            return this;
        }


        public Finder notContaining(String regex) {
            for (Iterator it = finders.iterator(); it.hasNext();) {
                Finder finder = (Finder)it.next();
                finder.notContaining(regex);
            }
            return this;
        }


        public boolean hasFound() {
            for (Iterator it = finders.iterator(); it.hasNext();) {
                Finder finder = (Finder)it.next();
                if (finder.hasFound()) {
                    return true;
                }
            }
            return false;
        }
    }
}
