# Modul450-Chess

Modified 10x10 chess variant with TDD. Movement tweaks:
- Lover piece next to the left rook; moves like king but does not give check.
- Extra rook on the far right; 10 pawns per side.
- Board is 10x10; bishops max 6 squares; queen up to 10; knights use 3+1 leap.

Run tests (includes JaCoCo):
```
mvn test
```

Launch simple console UI:
```
mvn spring-boot:run
```
Enter moves like `A2 A3` or `A2-A3`, `exit` to quit. The board display shows whose turn it is.
