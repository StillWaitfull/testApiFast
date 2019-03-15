package toolkit;

import config.ApplicationConfig;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static toolkit.RETROFITS.MAPPER;

public abstract class OperationsHelper {


    public static void sleep(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }


    public static String getRandomLogin() {
        long currentTime = System.nanoTime();
        String longNumber = String.valueOf(currentTime);
        return "login" + longNumber.substring(4, 9);
    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    public static String generateRandomEmail() {
        long currentTime = System.nanoTime();
        String longNumber = Long.toString(currentTime);
        return "notifytest." + longNumber + "@test.ru";
    }

    public static <T> Set<T> findDuplicates(Collection<T> list) {
        Set<T> duplicates = new LinkedHashSet<>();
        Set<T> uniques = new HashSet<>();
        for (T t : list) {
            if (!uniques.add(t)) {
                duplicates.add(t);
            }
        }
        return duplicates;
    }

    public static String getCurrentTimeStamp() {
        return String.valueOf(Instant.now().getEpochSecond());
    }

    public static <T> Response<T> delayAndExecuteRequest(Call<T> request, int waitTime) {
        try {
            sleep(waitTime);
            return request.execute();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка ввода вывода при запросе с задержкой ");
        }
    }

    public static <R> R checkAsyncRequest(Callable<R> call, Function<R, Boolean> func) {
        R result = null;
        try {
            for (int i = 0; i < 8; i++) {
                result = call.call();
                if (!func.apply(result)) sleep(ApplicationConfig.getInstance().PAUSE);
                else return result;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public static String generateUid() {
        return UUID.randomUUID().toString();
    }

    private static void waitAllResultsFromFeatures(List<CompletableFuture> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkMultiplyTimes(Runnable func, int count) {
        CompletableFuture[] futures = new CompletableFuture[count];
        try {
            for (int i = 0; i < count; i++) {
                futures[i] = CompletableFuture.runAsync(func);
            }
            CompletableFuture.allOf(futures).get();
            return true;
        } catch (ExecutionException | InterruptedException e) {
            return false;
        }
    }

    public static <T> Runnable executeRequestAndAssertWithResponseMessage(Call<T> request, List<Pair<Integer, String>> responses) {
        return () -> {
            try {
                Response<T> execute = request.clone().execute();
                ResponseBody errorBody = execute.errorBody();
                String responseText = Objects.isNull(errorBody) ? null : errorBody.string();
                assert responses.contains(new Pair<>(execute.code(), responseText)) :"Код и текст ответа на запрос " + OkClient.getRequest() + " ";
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public static <T> Runnable executeRequestAndAssertWithCodes(Call<T> request, List<Integer> responseCodes) {
        return () -> {
            try {
                Response<T> execute = request.clone().execute();
                assert responseCodes.contains(execute.code()) : "Код ответа на запрос " + OkClient.getRequest() + " " + execute.code();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }

    public static <T> Response<T> executeAsyncRequestWaitingValidCode(Call<T> request, int code) {
        return checkAsyncRequest(
                () -> request.clone().execute(),
                response -> response.code() == code
        );
    }


    public static boolean isJSONValid(String test) {
        boolean valid = true;
        try {
            MAPPER.readTree(test);
        } catch (IOException e) {
            valid = false;
        }
        return valid;
    }

    public static LocalDateTime convertTimestampToDate(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC);
    }

    public static long convertDateToTimestamp(LocalDateTime date) {
        return date.toEpochSecond(ZoneOffset.UTC);
    }

    public static <T extends Comparable<? super T>> boolean isSorted(Iterable<T> iterable, boolean asc) {
        Iterator<T> iter = iterable.iterator();
        if (!iter.hasNext()) {
            return true;
        }
        T t = iter.next();
        while (iter.hasNext()) {
            T t2 = iter.next();
            if (asc) {
                if (t.compareTo(t2) > 0) {
                    return false;
                }
            } else {
                if (t.compareTo(t2) < 0) {
                    return false;
                }
            }
            t = t2;
        }
        return true;
    }
}
