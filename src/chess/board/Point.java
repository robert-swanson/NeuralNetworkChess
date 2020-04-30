package chess.board;

/**
 * Describes a x and y position on the game board
 */
public class Point
{
	public int x;
	public int y;

	public Point(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	public Point(Point p)
	{
		this.x = p.x;
		this.y = p.y;
	}

	public Point getNewPoint(int dis, int dir)
	{
		Point rv = new Point(this);
		if (dir > 0 && dir < 4) // Going Right
			rv.x += dis;
		else if (dir > 4 && dir < 8) // Going Left
			rv.x -= dis;
		if (dir == 7 || dir == 0 || dir == 1) // Going Up
			rv.y -= dis;
		else if (dir > 2 && dir < 6) // Going Down
			rv.y += dis;
		return rv;
	}

	@Override
	public String toString()
	{
		return String.format("(%d, %d)", x, y);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Point))
			return false;
		Point p = (Point) obj;
		return (x == p.x && y == p.y);
	}

	@Override
	public int hashCode()
	{
		return x + y * 10;
	}

	/**
	 * Determines if the point is within the bounds of the board
	 * 
	 * @return true if the point is on the board
	 */
	public boolean isInBoard()
	{
		return (x >= 0 && x < 8 && y >= 0 && y < 8);
	}
}
