package db;

import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MemoryUserRepositoryTest {
    private final MemoryUserRepository repository = MemoryUserRepository.getInstance();

    private User user(String id) {
        return new User(id, "password", "Sample User", "sample@example.com");
    }

    @Test
    void storesAndFindsUsers() {
        User user = user(UUID.randomUUID().toString());
        repository.addUser(user);
        assertSame(user, repository.findUserById(user.getUserId()));
        assertTrue(repository.findAll().contains(user));
    }

    @Test
    void returnsNullForAnUnknownId() {
        assertNull(repository.findUserById(UUID.randomUUID().toString()));
    }

    @Test
    void replacesAUserWithTheSameId() {
        String id = UUID.randomUUID().toString();
        repository.addUser(user(id));
        User replacement = new User(id, "new-password", "New User", "new@example.com");
        repository.addUser(replacement);
        assertSame(replacement, repository.findUserById(id));
    }

    @Test
    void returnsADetachedUnmodifiableCollection() {
        User first = user(UUID.randomUUID().toString());
        repository.addUser(first);
        Collection<User> snapshot = repository.findAll();
        User later = user(UUID.randomUUID().toString());
        repository.addUser(later);
        assertTrue(snapshot.contains(first));
        assertFalse(snapshot.contains(later));
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        assertSame(first, repository.findUserById(first.getUserId()));
    }

    @Test
    void rejectsNullUsersAndIds() {
        assertThrows(NullPointerException.class, () -> repository.addUser(null));
        assertThrows(NullPointerException.class, () -> repository.addUser(user(null)));
        assertThrows(NullPointerException.class, () -> repository.findUserById(null));
    }

    @Test
    @Timeout(15)
    void sharesOneRepositoryAndKeepsConcurrentWrites() throws Exception {
        int workerCount = 8;
        int usersPerWorker = 100;
        String prefix = UUID.randomUUID() + "-";
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(workerCount);
        List<Future<?>> tasks = new ArrayList<>();
        try {
            for (int worker = 0; worker < workerCount; worker++) {
                int workerId = worker;
                tasks.add(executor.submit(() -> {
                    start.await();
                    assertSame(repository, MemoryUserRepository.getInstance());
                    for (int i = 0; i < usersPerWorker; i++) {
                        String id = prefix + workerId + "-" + i;
                        User user = user(id);
                        repository.addUser(user);
                        assertSame(user, repository.findUserById(id));
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get(10, TimeUnit.SECONDS);
            }
            for (int worker = 0; worker < workerCount; worker++) {
                for (int i = 0; i < usersPerWorker; i++) {
                    String id = prefix + worker + "-" + i;
                    assertEquals(user(id), repository.findUserById(id));
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
