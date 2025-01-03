import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.Set;
/*
public class Main {
    public static void main(String[] args) {
        double radius = 2.5; // радиус (число с плавающей запятой)

        // Создаем экземпляр DisplayMatrix


        // Вызываем методы экземпляра
        displayMatrix.printMatrix((int[][]) displayMatrix.placeDisplaysXxY(1, 6)[0]);
        int[] mat = (int[])displayMatrix.placeDisplaysXxY(6, 1)[2];
        System.out.println("n = " + mat[0] + " , m = " + mat[1]);
        displayMatrix.printMatrix((int[][]) displayMatrix.placeDisplaysXxY(6, 1)[0]);
    }
}
*/
public class Main {
	public static void main(String[] args) {
		//int n = 100; // количество строк
		//int m = 100; // количество столбцов
		double r = 11.5; // радиус с плавающей запятой
		DisplayMatrix displayMatrix = new DisplayMatrix(r);
		Object[] mat = displayMatrix.placeDisplaysXxY(4, 4);
		int n = ((int[])mat[2])[0];
		int m = ((int[])mat[2])[1];
		System.out.println("n - " + n + ", m - " + m);
		int[] processorsPerDisplay = new int[n * m];
		Arrays.fill(processorsPerDisplay, 0, n * m, 20); // количество процессоров для каждого дисплея
		int[][] displayCoordinates = (int[][])mat[1]; // координаты дисплеев
		int displaysCount = displayCoordinates.length;
		// Создаем экземпляр DisplayProcessorMatrix
		DisplayProcessorMatrix displayProcessorMatrix = new DisplayProcessorMatrix(n, m, displaysCount, processorsPerDisplay, displayCoordinates, r);
		displayProcessorMatrix.placeProcessors();
		// Создание изображения
		//displayProcessorMatrix.createImage("C:\\Users\\USER\\OneDrive\\Desktop\\test.png");
		displayProcessorMatrix.createImage("/storage/emulated/0/Download/1/matrix_image.png");
	}
}




class DisplayProcessorMatrix {
	private int n; // количество строк
	private int m; // количество столбцов
	private int k; // количество дисплеев
	private int[] processorsPerDisplay; // массив с количеством процессоров для каждого дисплея
	private int[][][] matrix; // матрица (тип клетки, индекс)
	private int displayIndex = 0; // начальный индекс дисплея
	private final int CELL_SIZE = 50; // размер клетки в пикселях
	private double RADIUS; // радиус для размещения процессоров

	public DisplayProcessorMatrix(int n, int m, int k, int[] processorsPerDisplay, int[][] displayCoordinates, double radius) {
		this.n = n;
		this.m = m;
		this.k = k;
		this.processorsPerDisplay = processorsPerDisplay;
		this.matrix = new int[n][m][2]; // [тип клетки, индекс]
		this.RADIUS = radius;
		// Инициализация матрицы
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				matrix[i][j][0] = 0; // клетка свободна
				matrix[i][j][1] = -1; // индекс -1 для свободной клетки
			}
		}

		// Размещение дисплеев
		placeDisplays(displayCoordinates);
		// Размещение процессоров

	}

	public void placeDisplays(int[][] displayCoordinates) {
		for (int[] coord : displayCoordinates) {
			int x = coord[0];
			int y = coord[1];

			// Проверка границ
			if (x >= 1 && x <= n - 2 && y >= 1 && y <= m - 2) {
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						matrix[x + i][y + j][0] = 2; // дисплей
						matrix[x + i][y + j][1] = displayIndex; // индекс дисплея
					}
				}
				displayIndex++;
			}
		}
	}

	public void placeProcessors() {
		for (int display = 0; display < k; display++) {
			final int currentDisplay = display; // Создаем финальную переменную
			int processorsToPlace = processorsPerDisplay[display];
			List<int[]> positions = generateEllipticalPositions(display);

			// Сортируем позиции по расстоянию до дисплея (по убыванию)
			positions.sort((pos1, pos2) -> {
				int dist1 = calculateDistanceToDisplay(pos1[0], pos1[1], currentDisplay);
				int dist2 = calculateDistanceToDisplay(pos2[0], pos2[1], currentDisplay);
				return Integer.compare(dist2, dist1); // Изменено на dist2, dist1
			});

			int placedCount = 0;
			// Счетчик размещенных процессоров

			for (int i = 0; i < processorsToPlace; i++) {
				boolean placed = false;

				// Сначала пробуем разместить процессор в заранее определенных позициях
				for (int[] pos : positions) {
					if (placeProcessor(pos[0], pos[1], currentDisplay)) {
						placed = true;
						placedCount++;
						break; // Выходим из цикла, если процессор успешно размещен
					}
				}

				// Если не удалось разместить, пробуем переместить процессоры
				if (!placed) {
					HashSet<String> checkedPositions = new HashSet<>();
					for (int[] pos : positions) {
						if (moveProcessor(pos[0], pos[1], checkedPositions)) {
							placeProcessor(pos[0], pos[1], currentDisplay);
							placed = true;
							placedCount++;
							break; // Выходим из цикла, если процессор успешно размещен
						}
					}
				}

				// Если процессор не был размещен, выводим сообщение и выходим
				if (!placed) {
					System.out.println("Не удалось разместить процессор для дисплея " + (currentDisplay + 1));
					break; // Выход из цикла, если не удалось разместить процессор
				}
			}

			// Проверяем, удалось ли разместить все процессоры
			if (placedCount < processorsToPlace) {
				System.out.println("Не удалось разместить все процессоры для дисплея " + (currentDisplay + 1) + ". Размещено: " + placedCount);
			}
		}
		SortProcessors(k);
		SortProcessors(k);

	}

	public int[][] GetProcessorsCords(int DisplayIndex) {

		List<int[]> positions = generateEllipticalPositions(DisplayIndex);

		List<int[]> ProcessorPositions = new ArrayList<>();
		for (int[] pos : positions) {
			//System.out.println("" + matrix[pos[0]][pos[1]][1] + " + " + DisplayIndex);
			if (matrix[pos[0]][pos[1]][0] != 2 && matrix[pos[0]][pos[1]][1] == DisplayIndex + 1) {
				ProcessorPositions.add(pos);
			}
		}
		// Преобразуем List<int[]> в int[][]
		int[][] array = new int[ProcessorPositions.size()][];
		for (int i = 0; i < ProcessorPositions.size(); i++) {
			array[i] = ProcessorPositions.get(i); // Копируем каждый массив
		}
		System.out.println("" + ProcessorPositions.size());
		return array;
	}

	private void SortProcessors(int DisplayCount) {
		int mid = DisplayCount / 2; // Середина

		for (int i = 0; i < mid; i++) {
			// Сначала обрабатываем дисплей в середине
			int currentDisplay = mid - 1 - i; // Индексы от 7 до 0
			processDisplay(currentDisplay);

			// Затем обрабатываем дисплей справа от середины
			if (mid + i < DisplayCount) {
				currentDisplay = mid + i; // Индексы от 8 до 15
				processDisplay(currentDisplay);
			}
		}
	}
	private void processDisplay(int currentDisplay) {
		// Ваш оригинальный код, который использует currentDisplay
		List<int[]> positions = generateEllipticalPositions(currentDisplay);

		// Сортируем позиции по расстоянию до дисплея (по убыванию)
		positions.sort((pos1, pos2) -> {
			int dist1 = calculateDistanceToDisplay(pos1[0], pos1[1], currentDisplay);
			int dist2 = calculateDistanceToDisplay(pos2[0], pos2[1], currentDisplay);
			return Integer.compare(dist1, dist2);
		});

		// Счетчик размещенных процессоров
		int[][] ProcPositions = GetProcessorsCords(currentDisplay);
		for (int i = 0; i < ProcPositions.length; i++) {
			for (int[] pos : positions) {
				if (isValidPositionMass(pos)) {
					int dist1 = calculateDistanceToDisplay(pos[0], pos[1], currentDisplay);
					int dist2 = calculateDistanceToDisplay(ProcPositions[i][0], ProcPositions[i][1], currentDisplay);
					if (dist1 >= dist2) {
						continue;
					}
					placeProcessor(pos[0], pos[1], currentDisplay);
					matrix[ProcPositions[i][0]][ProcPositions[i][1]] = new int[] {0, -1};
					break; // Выходим из цикла, если процессор успешно размещен
				}
			}
		}
	}
private boolean moveProcessor(int processorX, int processorY, HashSet<String> checkedPositions) {
	// Формируем строку для уникальной идентификации позиции
	String positionKey = processorX + "," + processorY;

	// Проверяем, была ли уже проверена эта позиция
	if (checkedPositions.contains(positionKey)) {
		return false; // Если уже проверяли, пропускаем
	}
	checkedPositions.add(positionKey); // Добавляем позицию в проверенные

	// Получаем индекс дисплея, к которому принадлежит процессор
	int displayIndex = matrix[processorX][processorY][1] - 1;

	// Генерируем позиции в форме круга для текущего дисплея
	List<int[]> positions = generateEllipticalPositions(displayIndex);
	boolean moved = false;

	// Перебираем все позиции в окружности дисплея
	for (int[] pos : positions) {
		int newX = pos[0];
		int newY = pos[1];

		// Проверяем, что новая позиция допустима и не является позицией дисплея
		if (isValidPosition(newX, newY) && matrix[newX][newY][0] != 2) {
			// Перемещаем процессор
			if (placeProcessor(newX, newY, displayIndex)) {
				// Освобождаем старую позицию
				matrix[processorX][processorY][0] = 0; // Освобождаем старую позицию
				matrix[processorX][processorY][1] = -1; // Индекс -1 для свободной клетки
				moved = true; // Установим флаг перемещения
				break; // Успешно переместили, выходим из цикла
			}
		}
	}

	// Если не удалось переместить, пробуем переместить на координаты другого процессора
	if (!moved) {
		for (int i = 0; i < n; i++) { // Перебираем все процессоры
			for (int j = 0; j < m; j++) {
				if (matrix[i][j][0] == 1 && (i != processorX || j != processorY)) { // Если это процессор и не тот же самый
					// Рекурсивно пытаемся переместить другой процессор
					if (moveProcessor(i, j, checkedPositions)) {
						// После успешного перемещения другого процессора, пробуем переместить текущий процессор
						return moveProcessor(processorX, processorY, checkedPositions);
					}
				}
			}
		}
	}

	return moved; // Возвращаем результат перемещения
}



private boolean isValidPosition(int x, int y) {
	return x >= 0 && x < n && y >= 0 && y < m && matrix[x][y][0] == 0; // Проверка границ матрицы
}
private boolean isValidPositionMass(int[] pos) {
	int x = pos[0];
	int y = pos[1];
	return x >= 0 && x < n && y >= 0 && y < m && matrix[x][y][0] == 0; // Проверка границ матрицы
}
private List<int[]> generateEllipticalPositions(int displayIndex) {
	double radius = RADIUS; // Используем double для радиуса
	Set<String> uniquePositions = new HashSet<>();
	List<int[]> displayPositions = new ArrayList<>();

	// Находим все позиции дисплея
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			if (matrix[i][j][0] == 2 && matrix[i][j][1] == displayIndex) {
				displayPositions.add(new int[] {i, j});
			}
		}
	}
	// Генерация позиций в форме круга для каждой позиции дисплея
	for (int[] displayPos : displayPositions) {
		int centerX = displayPos[0];
		int centerY = displayPos[1];

		for (int x = (int) - radius; x <= radius; x++) {
			for (int y = (int) - radius; y <= radius; y++) {
				if (x * x + y * y <= radius * radius) {
					int posX = centerX + x;
					int posY = centerY + y;
					if (posX >= 0 && posX < n && posY >= 0 && posY < m && matrix[posX][posY][0] != 2 && isWithinDisplayRadius(posX, posY, displayIndex)) {
						// Добавляем позицию в Set в виде строки для уникальности
						uniquePositions.add(posX + "," + posY);
					}
				}
			}
		}
	}

	// Преобразуем Set обратно в список
	List<int[]> positions = new ArrayList<>();
	for (String pos : uniquePositions) {
		String[] coords = pos.split(",");
		positions.add(new int[] {Integer.parseInt(coords[0]), Integer.parseInt(coords[1])});
	}

	return positions;
}

private boolean placeProcessor(int x, int y, int display) {
	if (matrix[x][y][0] == 0 && isWithinDisplayRadius(x, y, display) && !isBlocked(x, y, display)) {
		matrix[x][y][0] = 1; // процессор
		matrix[x][y][1] = display + 1; // индекс дисплея (начиная с 1)
		return true;
	}
	return false;
}

private boolean isWithinDisplayRadius(int x, int y, int display) {
	// Проверка, находится ли позиция в пределах радиуса дисплея
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			if (matrix[i][j][0] == 2 && matrix[i][j][1] == display) {
				i++;
				j++;
				return Math.sqrt((x - i) * (x - i) + (y - j) * (y - j)) <= RADIUS;
			}
		}
	}
	return false;
}

private boolean isBlocked(int x, int y, int display) {
	// Проверка, заблокирована ли позиция
	return matrix[x][y][0] != 0; // если клетка не свободна
}

private int calculateDistanceToDisplay(int x, int y, int displayIndex) {
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			if (matrix[i][j][0] == 2 && matrix[i][j][1] == displayIndex) { // если это дисплей
				i++;
				j++;
				return (x - i) * (x - i) + (y - j) * (y - j); // квадрат расстояния
			}
		}
	}
	return Integer.MAX_VALUE; // если дисплей не найден
}
private static void outmat(int[][][] matrix) {
	for (int[][] row : matrix) {
		for (int[] pos : row) {
			System.out.print(" (" + pos[0] + "," + pos[1] + ") ");
		}
		System.out.println();
	}
}
public void createImage(String filePath) {
	BufferedImage image = new BufferedImage(m * CELL_SIZE, n * CELL_SIZE, BufferedImage.TYPE_INT_RGB);
	Graphics g = image.getGraphics();

	// Определяем 100 уникальных цветов для дисплеев
	Set<Color> uniqueColors = new HashSet<>();
	while (uniqueColors.size() < 100) {
		uniqueColors.add(new Color((int)(Math.random() * 0x1001001))); // Генерация случайного цвета
	}
	Color[] displayColors = uniqueColors.toArray(new Color[0]);

	// Счетчики для цветов
	int[] colorCounts = new int[displayColors.length + 2]; // +2 для белого и красного

	// Рисуем матрицу
	for (int i = 0; i < n; i++) {
		for (int j = 0; j < m; j++) {
			if (matrix[i][j][0] == 0) {
				g.setColor(Color.WHITE); // свободная клетка
				colorCounts[displayColors.length]++; // Увеличиваем счетчик для белого
			} else if (matrix[i][j][0] == 1) {
				int displayIndex = matrix[i][j][1] - 1; // Получаем индекс дисплея (уменьшаем на 1 для соответствия)
				if (displayIndex >= 0 && displayIndex < displayColors.length) {
					g.setColor(displayColors[displayIndex]); // Устанавливаем цвет процессора в зависимости от дисплея
					colorCounts[displayIndex]++; // Увеличиваем счетчик для соответствующего цвета
				}
			} else if (matrix[i][j][0] == 2) {
				int displayIndex = matrix[i][j][1];
				g.setColor(Color.RED); // дисплей
				colorCounts[displayColors.length + 1]++; // Увеличиваем счетчик для красного
			}
			g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
		}
	}

	g.dispose();
	try {
		ImageIO.write(image, "png", new File(filePath));
	} catch (IOException e) {
		e.printStackTrace();
	}

	// Выводим результаты в консоль
	System.out.println("Количество покрашенных клеток:");
	for (int i = 0; i < displayColors.length; i++) {
		if (colorCounts[i] == 0)
			continue;
		System.out.println(displayColors[i] + ": " + colorCounts[i] + "||	" + i);
	}
	System.out.println("Белые клетки: " + colorCounts[displayColors.length]);
	System.out.println("Красные клетки (дисплеи): " + colorCounts[displayColors.length + 1]);
}
}


class DisplayMatrix {

	private int radius;

	// Конструктор должен быть public, чтобы его можно было вызывать из Main
	public DisplayMatrix(double radius) {
		this.radius = (int) Math.round(radius);
	}

	// Изменяем модификатор доступа на public
	public void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int value : row) {
				System.out.print(value + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

	private int[][] getOffsets(int[][] matrix, int displayCol, int displayCount) {
		int[][] offsets = new int[displayCount][2];

		int count = 0;
		for (int i = radius + 2; i < matrix.length && count < displayCount; i += 3) {
			for (int j = radius + 2; j < matrix[0].length && count < displayCount; j += 3) {
				// Проверяем, не превышает ли количество колонок
				if ((j - (radius + 2)) / 3 >= displayCol) {
					break; // Прерываем внутренний цикл, если достигли максимального количества колонок
				}
				offsets[count++] = new int[] {i, j};
			}
		}
		return offsets;
	}

	private void placeDisplaysXxX(int[][] matrix, int[][] centers, int row, int col) {
		// Смещения для размещения дисплеев
		int[][] offsets = getOffsets(matrix, col, row * col);
		for (int d = 0; d < row * col; d++) {
			centers[d][0] = offsets[d][0];
			centers[d][1] = offsets[d][1];

			for (int i = centers[d][0] - 1; i <= centers[d][0] + 1; i++) {
				for (int j = centers[d][1] - 1; j <= centers[d][1] + 1; j++) {
					if (i >= 0 && i < matrix.length && j >= 0 && j < matrix[0].length) {
						matrix[i][j] = 1; // Заполняем матрицу
					}
				}
			}
		}
	}

	public Object[] placeDisplaysXxY(int x, int y) {
		// Вычисляем размеры матрицы
		int n = 2 * radius + 2 + y * 3; // количество строк
		int m = 2 * radius + 2 + x * 3; // количество столбцов
		// Создаем матрицу n на m, заполненную нулями
		int[][] matrix = new int[n][m];

		// Массив для хранения координат центров дисплеев
		int[][] centers = new int[y * x][2];
		placeDisplaysXxX(matrix, centers, y, x);
		return new Object[] {matrix, centers, new int[] {n, m}};
	}
}
