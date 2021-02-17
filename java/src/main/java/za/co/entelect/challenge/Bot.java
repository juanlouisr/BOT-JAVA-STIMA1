package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;
    private final Position center = new Position(16, 16);


    public void printnih() {
        System.out.println(currentWorm.id);
    }

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);


//        gameState.myPlayer.worms[id].position
    }


    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }



    public Command run() {
        Cell blockTemp = cellTerjauh3(currentWorm.position, opponent);
        Cell block = cellTerdekat(currentWorm.position.x, currentWorm.position.y, blockTemp.x, blockTemp.y);

        // Mencatat hal-hal penting
        final Worm enemyWorm = getFirstWormInRange(currentWorm.position.x, currentWorm.position.y);
        final List<MyWorm> otherWorms = Arrays.stream(gameState.myPlayer.worms).filter(worm -> worm != currentWorm && worm.health > 0).collect(Collectors.toList());
        final List<MyWorm> temenSekitarSekarang = getSurroundingAlly(currentWorm.position.x, currentWorm.position.y, 4);
        final List<Worm> cacingMusuhDisekitar = getSurroundingWorm(currentWorm.position.x, currentWorm.position.y, 4);
        final List<Cell> cellPelarian = runawayBlock(currentWorm.position.x, currentWorm.position.y);
        final List<Cell> cellHuntMusuh = cellUntukMenembak(currentWorm.position.x, currentWorm.position.y);


        // Menjauh dari Lava strategy
        if (disekitarLava(currentWorm.position.x, currentWorm.position.y)) {
            System.out.println("disekitar lava");
            Cell menujuPusatBumi = cellTerdekat(currentWorm.position.x, currentWorm.position.y, center.x, center.y);
            return galiAtoGerakAtoDiem(menujuPusatBumi);
        }


        // Special Power Strategy
        System.out.println("jumlah musuh sekitar: " + cacingMusuhDisekitar.size());
        for (Worm worm : cacingMusuhDisekitar) {
            if (currentWorm.canBananaBomb()) {
                return new BananaBombCommand(worm.position.x, worm.position.y);
            } else if (currentWorm.canSnowBalls()) {
                if (!worm.isFrozen()) {
                    return new SnowBallCommand(worm.position.x, worm.position.y);
                }
            }
        }


        // Jika Menang skor farming atau kabur-kaburan (late game)
        if (gameState.myPlayer.score > opponent.score) {
            System.out.println("menang skor");

            if (cacingMusuhDisekitar.size() > 1 && temenSekitarSekarang.size() == 0) {
                // run strategy
                for (Cell cell : cellPelarian) {
                    if (cell.type == CellType.AIR) {
                        return new MoveCommand(cell.x, cell.y);
                    }
                }
            }

            // Farm startegy jika round masih < 300
            if (gameState.currentRound <= 300) {
                if (cacingMusuhDisekitar.size() >= 1) {
                    int totalDarahMusuh = 0;
                    for (Worm worm : cacingMusuhDisekitar) {
                        totalDarahMusuh += worm.health;
                    }

                    if (currentWorm.health > totalDarahMusuh) {
                        // Berburu Musuh
                        if (enemyWorm == null) {
                            for (Cell cell : cellHuntMusuh) {
                                if (cell.type == CellType.AIR) {
                                    return new MoveCommand(cell.x, cell.y);
                                }
                            }
                        }
                        // Jika ada musuh dalam jarak pandang dan musuh sendirian, tembak saja
                        else {
                            if (enemyWorm.health > 0) {
                                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                                return new ShootCommand(direction);
                            }
                        }
                    }


                }
                // Farming jika sendirian
                else {
                    Command farmDay = farming(currentWorm.position.x, currentWorm.position.y);
                    if ((enemyWorm == null || enemyWorm.health <= 0) && farmDay != null) {
                        return farmDay;
                    }
                }

            }
            // Memasuki late game (menjadi pelari) / Darah tinggal sekarat
            // run strategy
            for (Cell cell : cellPelarian) {
                if (cell.type == CellType.AIR) {
                    return new MoveCommand(cell.x, cell.y);
                }
            }

            List<Cell> cellSekitar = getSurroundingCells(currentWorm.position.x,currentWorm.position.y);
            for (Cell cell : cellSekitar){
                if (cell.type == CellType.AIR) {
                    return new MoveCommand(cell.x, cell.y);
                }
            }

            if (enemyWorm != null && enemyWorm.health > 0) {
                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                return new ShootCommand(direction);
            }
        }


        // Jika kalah skor maka baku hantam
        else {
            System.out.println("kalah skor");
            // Jika ada musuh solo di sekitar, buru dia
            if (enemyWorm == null && cacingMusuhDisekitar.size() == 1) {
                for (Cell cell : cellHuntMusuh) {
                    if (cell.type == CellType.AIR) {
                        System.out.println("musuh nyasar, berburu (angin)");
                        return new MoveCommand(cell.x, cell.y);
                    }
                }
                for (Cell cell : cellHuntMusuh) {
                    if (cell.type == CellType.DIRT) {
                        System.out.println("musuh nyasar, berburu (tanah)");
                        return new DigCommand(cell.x, cell.y);
                    }
                }
            }


            // Jika ada musuh dalam jarak pandang dan musuh sendirian, tembak saja
            if (enemyWorm != null && cacingMusuhDisekitar.size() == 1) {
                if (enemyWorm.health > 0) {
                    System.out.println("musuh nyasar tembak!!!");
                    Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                    return new ShootCommand(direction);
                }
            }

            // Jika masih ada teman yang hidup
            if (otherWorms.size() > 0) {
                // Membantu teman strategy
                // Bisa select maka select (select hanya dilakukan ketika tidak ada musuh dalam pandangan)
                if (enemyWorm == null) {
                    for (MyWorm worm : otherWorms) {
                        List<Worm> cacingMusuhSekitarTeman = getSurroundingWorm(worm.position.x, worm.position.y, 5);
                        if (gameState.myPlayer.remainingWormSelections > 0) {
                            for (Worm enemyworm : cacingMusuhSekitarTeman) {
                                if (worm.canBananaBomb()) {
                                    return new SelectCommand(worm.id, new BananaBombCommand(enemyworm.position.x, enemyworm.position.y));
                                }
                            }

                            Worm botMusuh = getFirstWormInRange(worm.position.x, worm.position.y);
                            if (botMusuh != null && botMusuh.health > 0) {
                                System.out.println("select shoot");
                                Direction direksiUtama = resolveDirection(worm.position, botMusuh.position);
                                return new SelectCommand(worm.id, new ShootCommand(direksiUtama));
                            }

                        }

                        List<MyWorm> TemanSekitarCacing = getSurroundingAlly(worm.position.x, worm.position.y, 4);
                        if (cacingMusuhSekitarTeman.size() >= 2
                                && TemanSekitarCacing.size() <= 1
                                && euclideanDistance(currentWorm.position.x, currentWorm.position.y, worm.position.x, worm.position.y) >= 4) {
                            System.out.println("menuju ke temen");
                            Cell cellKeTemen = cellTerdekat(currentWorm.position.x, currentWorm.position.y, worm.position.x, worm.position.y);
                            return galiAtoGerakAtoDiem(cellKeTemen);
                        }

                    }
                }
                else {
                    // Jika musuh disektar lebih 1 dan tidak ada teman di sekitar, kabur saja
                    if (cacingMusuhDisekitar.size() > 1 && temenSekitarSekarang.size() == 0) {
                        for (Cell cell : cellPelarian) {
                            if (cell.type == CellType.AIR) {
                                // Menghindar sampai bala bantuan datang
                                return new MoveCommand(cell.x, cell.y);
                            }
                        }
                        for (Cell cell : cellPelarian) {
                            if (cell.type == CellType.DIRT) {
                                // Menghindar sampai bala bantuan datang
                                return new DigCommand(cell.x, cell.y);
                            }
                        }
                    }

                    if (cacingMusuhDisekitar.size() >= 2) {
                        if (temenSekitarSekarang.size() >= 1) {
                            System.out.println("22222222222222");
                            if (enemyWorm.health > 0) {
                                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                                return new ShootCommand(direction);
                            }
                            for (Cell cell : cellHuntMusuh) {
                                if (cell.type == CellType.AIR) {
                                    return new MoveCommand(cell.x, cell.y);
                                }
                            }
                            for (Cell cell : cellHuntMusuh) {
                                if (cell.type == CellType.DIRT) {
                                    return new DigCommand(cell.x, cell.y);
                                }
                            }
                        }
                    }
                    if (enemyWorm.health > 0) {
                        Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                        return new ShootCommand(direction);
                    }
                }
            }

            // Jika sisa sendiri
            if (otherWorms.size() == 0) {
                if (cacingMusuhDisekitar.size() > 1) {
                    int totalDarahMusuh = 0;
                    for (Worm worm : cacingMusuhDisekitar) {
                        totalDarahMusuh += worm.health;
                    }
                    if (totalDarahMusuh <= currentWorm.health) {
                        if (enemyWorm != null) {
                            if (enemyWorm.health > 0) {
                                Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                                return new ShootCommand(direction);
                            }
                        }
                        for (Cell cell : cellHuntMusuh) {
                            if (cell.type == CellType.AIR) {
                                return new MoveCommand(cell.x, cell.y);
                            }
                        }
                        for (Cell cell : cellHuntMusuh) {
                            if (cell.type == CellType.DIRT) {
                                return new DigCommand(cell.x, cell.y);
                            }
                        }
                    }
                    else {
                        // Menghindar sampai datang keajaiban
                        for (Cell cell : cellPelarian) {
                            if (cell.type == CellType.AIR) {
                                // Menghindar sampai bala bantuan datang
                                return new MoveCommand(cell.x, cell.y);
                            }
                        }
                        for (Cell cell : cellPelarian) {
                            if (cell.type == CellType.DIRT) {
                                // Menghindar sampai bala bantuan datang
                                return new DigCommand(cell.x, cell.y);
                            }
                        }
                    }
                }
                // Jika 1 v 1: pasrahkan saja pada rng
                if (enemyWorm != null) {
                    if (enemyWorm.health > 0) {
                        Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                        return new ShootCommand(direction);
                    }
                }
                for (Cell cell : cellHuntMusuh) {
                    if (cell.type == CellType.AIR) {
                        return new MoveCommand(cell.x, cell.y);
                    }
                }
                for (Cell cell : cellHuntMusuh) {
                    if (cell.type == CellType.DIRT) {
                        // Menghindar sampai bala bantuan datang
                        return new DigCommand(cell.x, cell.y);
                    }
                }
            }
        }

        Command farmDay = farming(currentWorm.position.x, currentWorm.position.y);
        if (enemyWorm != null && enemyWorm.health <= 0 && farmDay != null) {
            return farmDay;
        }
        if (enemyWorm == null && farmDay != null) {
            return farmDay;
        }
        System.out.println("gagal farming");
        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
//        System.out.println("Panjang block list cell: "+ surroundingBlocks.size());
        int cellIdx = random.nextInt(surroundingBlocks.size());
        block = surroundingBlocks.get(cellIdx);
        System.out.println("gerak random");
        return galiAtoGerakAtoDiem(block);

    }


    private boolean disekitarLava(int x, int y) {
        for (int i = x - 1; i < x + 1; i++) {
            for (int j = y - 1; j < y + 1; j++) {
                if (isValidCoordinate(i, j)) {
                    if (gameState.map[j][i].type == CellType.LAVA) {
                        System.out.println("disekitar lava");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<Cell> runawayBlock(int x, int y) {
        List<Cell> cellPelarian = new ArrayList<>();
        for (int i = x - 1; i < x + 1; i++) {
            for (int j = y - 1; j < y + 1; j++) {
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    Worm wormMusuh = getFirstWormInRange(i, j);
                    if ((wormMusuh == null)
                            && gameState.map[j][i].occupier == null
                            && (gameState.map[j][i].type != CellType.LAVA)
                            && (gameState.map[j][i].type != CellType.DEEP_SPACE))
                        cellPelarian.add(gameState.map[j][i]);
                }
            }
        }
        return cellPelarian;
    }


    private List<Cell> cellUntukMenembak(int x, int y) {
        List<Cell> cellPenembakan = new ArrayList<>();
        for (int i = x - 1; i < x + 1; i++) {
            for (int j = y - 1; j < y + 1; j++) {
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    Worm wormMusuh = getFirstWormInRange(i, j);
                    if ((wormMusuh != null)
                            && (gameState.map[j][i].type != CellType.LAVA)
                            && (gameState.map[j][i].type != CellType.DEEP_SPACE))
                        cellPenembakan.add(gameState.map[j][i]);
                }
            }
        }
        return cellPenembakan;
    }


    private Command galiAtoGerakAtoDiem(Cell block) {
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        }
        if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }
        if (block.type == CellType.LAVA) {
            return new MoveCommand(block.x, block.y);
        }
        return new DoNothingCommand();
    }

    // Algoritma farming
    private Command farming(int x, int y) {
        List<Cell> cellSekitar = getSurroundingCells(x, y);
        List<CellWithDistance> cellSekitar5 = new ArrayList<>();
//        List<Cell> cellPelarian = runawayBlock(currentWorm.position.x,currentWorm.position.y);
        for (int i = x - 5; i < x + 5; i++) {
            for (int j = y - 5; j < y + 5; j++) {
                int dirtCounter = 0;
                for (int k = i - 1; k < i + 1; k++) {
                    for (int l = j - 1; l < j + 1; l++) {
                        if (isValidCoordinate(k, l) && gameState.map[l][k].type == CellType.DIRT) {
                            dirtCounter += 1;
                        }
                    }
                }

                if (dirtCounter >= 3 && isValidCoordinate(i, j)) {
                    cellSekitar5.add(new CellWithDistance(gameState.map[j][i], euclideanDistance(x, y, gameState.map[j][i].x, gameState.map[j][i].y)));
                }
            }
        }
        for (Cell cell : cellSekitar) {
            Worm musuh = getFirstWormInRange(x, y);
            if (musuh == null) {
                if (cell.type == CellType.DIRT) {
                    System.out.println("Farming dlu");
                    return new DigCommand(cell.x, cell.y);
                }
            }
        }
        Collections.sort(cellSekitar5);
        for (CellWithDistance cwd : cellSekitar5) {
            Cell terdekat = cellTerdekat(x, y, cwd.cell.x, cwd.cell.y);
            System.out.println("cari dirt");
            return galiAtoGerakAtoDiem(terdekat);
        }
        return null;
    }


    // Worm Pertama terdekat
    private Worm getFirstWormInRange(int x, int y) {
        Set<String> cells = constructFireDirectionLines(x, y, currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }
        return null;
    }

    // Mengembalikan cacing di sekitar kita
    private List<Worm> getSurroundingWorm(int x, int y, int range) {
        ArrayList<Worm> cacingsMusuh = new ArrayList<>();
        for (int i = x - range; i <= x + range; i++) {
            for (int j = y - range; j <= y + range; j++) {
                // Don't include the current position
                if (i != x && j != y
                        && isValidCoordinate(i, j)
                        && euclideanDistance(i, j, x, y) <= range
                        && gameState.map[j][i].occupier != null
                        && gameState.map[j][i].occupier.playerId != gameState.myPlayer.id
                        && gameState.map[j][i].occupier.health > 0) {
                    if (gameState.map[j][i].occupier.health > 0)
                        cacingsMusuh.add(opponent.worms[gameState.map[j][i].occupier.id - 1]);
                }
            }
        }
        return cacingsMusuh;
    }

    private List<MyWorm> getSurroundingAlly(int x, int y, int range) {
        ArrayList<MyWorm> cacingsTemen = new ArrayList<>();
        for (int i = x - range; i <= x + range; i++) {
            for (int j = y - range; j <= y + range; j++) {
                // Don't include the current position
                if (i != x && j != y
                        && isValidCoordinate(i, j)
                        && euclideanDistance(i, j, x, y) <= range
                        && gameState.map[j][i].occupier != null
                        && gameState.map[j][i].occupier.playerId == gameState.myPlayer.id
                        && gameState.map[j][i].occupier.health > 0) {
                    if (gameState.map[j][i].occupier.health > 0)
                        cacingsTemen.add(gameState.myPlayer.worms[gameState.map[j][i].occupier.id - 1]);
                }
            }
        }
        return cacingsTemen;
    }


    // Mengembalikan jarak dari posisi ke suatu worm jika worm tersebut hidup, jika mati, mengembalikkan 0
    private int penghitungJarak(int x, int y, Worm worm) {
        if (worm.health > 0) {
            return euclideanDistance(x, y, worm.position.x, worm.position.y);
        }
        return 0;
    }


    private List<List<Cell>> constructFireDirectionLines(int x, int y, int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = x + (directionMultiplier * direction.x);
                int coordinateY = y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(x, y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells6(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 10; i <= x + 10; i++) {
            for (int j = y - 10; j <= y + 10; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && gameState.map[j][i].occupier == null) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }


    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j) && gameState.map[j][i].occupier == null) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    // Mencari cell terdeket untuk ke posisi tujuan
    private Cell cellTerdekat(int x1, int y1, int x2, int y2) {
        List<CellWithDistance> hidupku = getSurroundingCells(x1, y1).stream()
                .map(C -> new CellWithDistance(C, euclideanDistance(C.x, C.y, x2, y2)))
                .collect(Collectors.toList());
        Collections.sort((hidupku));
        for (CellWithDistance cellWithDistance : hidupku) {
            if (cellWithDistance.cell.type != CellType.LAVA
                    && cellWithDistance.cell.type != CellType.DEEP_SPACE
                    && cellWithDistance.cell.occupier == null)
                return cellWithDistance.cell;
        }

        return hidupku.get(0).cell;
    }

    // Mencari Cell sekitar yang terjauh dari ketiga musuh dan cell bukan lava
    private Cell cellTerjauh3(Position origin, Opponent opponents) {
        List<CellTerjauhDari3Musuh> cell3 = getSurroundingCells6(origin.x, origin.y).stream()
                .map(C -> new CellTerjauhDari3Musuh(C, penghitungJarak(C.x, C.y
                        , opponents.worms[0]), penghitungJarak(C.x, C.y, opponents.worms[1]), penghitungJarak(C.x, C.y,
                        opponents.worms[2]))).sorted().collect(Collectors.toList());
        for (CellTerjauhDari3Musuh cellTerjauhDari3Musuh : cell3) {
            if (cellTerjauhDari3Musuh.cell.type != CellType.LAVA && cellTerjauhDari3Musuh.cell.type != CellType.DEEP_SPACE)
                return cellTerjauhDari3Musuh.cell;
        }
        return cell3.get(0).cell;
    }


    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private void updateBahaya(MyWorm worm){
        List<Worm> cacingMusuhDisekitar = getSurroundingWorm(worm.position.x, worm.position.y, 4);
        worm.dalambahaya = cacingMusuhDisekitar.size() >= 2;
    }


    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        //
        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }
}
