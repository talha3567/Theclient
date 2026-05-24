package meteordevelopment.meteorclient.gui.screens.settings.base;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.core.IdMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SortingHelper {
    private static final Comparator<Entry<?>> FILTER_COMPARATOR = Comparator.comparingInt(Entry::distance);

    private SortingHelper() {
    }

    public static <T> Iterable<T> sort(Iterable<T> registry, Predicate<T> filter, Function<T, String[]> nameFunction, String filterText) {
        return SortingHelper.sortInternal(registry, filter, nameFunction, filterText, null);
    }

    public static <T> Iterable<T> sortWithPriority(Iterable<T> registry, Predicate<T> filter, Function<T, String[]> nameFunction, String filterText, Comparator<T> comparator) {
        return SortingHelper.sortInternal(registry, filter, nameFunction, filterText, comparator);
    }

    private static <T> Iterable<T> sortInternal(Iterable<T> registry, Predicate<T> filter, Function<T, String[]> nameFunction, String filterText, @Nullable Comparator<T> comparator) {
        if (filterText.isBlank()) {
            if (comparator == null) {
                return SortingHelper.filtering(registry, filter);
            }
            List<T> list = SortingHelper.createList(registry);
            for (T value : registry) {
                if (!filter.test(value)) continue;
                list.add(value);
            }
            list.sort(comparator);
            return list;
        }
        List<Entry<T>> list = SortingHelper.createList(registry);
        for (T value : registry) {
            if (!filter.test(value)) continue;
            String[] names = nameFunction.apply(value);
            int bestWords = 0;
            int bestDistance = Integer.MAX_VALUE;
            float relevancy = 0.0f;
            for (String name : names) {
                int words = Utils.searchInWords(name, filterText);
                int distance = Utils.searchLevenshteinDefault(name, filterText, false);
                bestWords = Math.max(bestWords, words);
                bestDistance = Math.min(bestDistance, distance);
                relevancy = Math.max(relevancy, 1.0f - (float)distance / (float)name.length());
            }
            if (bestWords <= 0 && !(relevancy >= 0.5f)) continue;
            list.add(new Entry<T>(value, bestDistance));
        }
        Comparator<Entry<T>> entryComparator = comparator != null ? Comparator.comparing(Entry::value, comparator).thenComparing(SortingHelper.filterComparator()) : SortingHelper.filterComparator();
        list.sort(entryComparator);
        return SortingHelper.iterate(list);
    }

    private static <T> List<T> createList(Iterable<?> iterable) {
        Iterable<?> iterable2 = iterable;
        Objects.requireNonNull(iterable2);
        Iterable<?> iterable3 = iterable2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{IdMap.class, Collection.class}, iterable3, n)) {
            case 0 -> {
                IdMap indexed = (IdMap)iterable3;
                yield new ObjectArrayList(indexed.size());
            }
            case 1 -> {
                Collection collection = (Collection)iterable3;
                yield new ObjectArrayList(collection.size());
            }
            default -> new ObjectArrayList();
        };
    }

    private static <T> Comparator<Entry<T>> filterComparator() {
        return FILTER_COMPARATOR;
    }

    private static <T> Iterable<T> iterate(final List<Entry<T>> sortedList) {
        return new Iterable<T>(){

            @Override
            @NotNull
            public Iterator<T> iterator() {
                return new Iterator<T>(this){
                    private final Iterator<Entry<T>> it;
                    {
                        Objects.requireNonNull(this$0);
                        this.it = sortedList.iterator();
                    }

                    @Override
                    public boolean hasNext() {
                        return this.it.hasNext();
                    }

                    @Override
                    public T next() {
                        return this.it.next().value();
                    }
                };
            }
        };
    }

    private static <T> Iterable<T> filtering(final Iterable<T> iterable, final Predicate<T> filter) {
        return new Iterable<T>(){

            @Override
            @NotNull
            public Iterator<T> iterator() {
                throw new UnsupportedOperationException("iterator() not supported by this Iterable, use forEach() instead.");
            }

            @Override
            public void forEach(Consumer<? super T> action) {
                for (Object value : iterable) {
                    if (!filter.test(value)) continue;
                    action.accept(value);
                }
            }
        };
    }

    public record Entry<T>(T value, int distance) {
    }
}
