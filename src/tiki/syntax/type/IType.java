package tiki.syntax.type;

public interface IType {
	public String description();

	public IType getBase();

	public Kind getKind();

	public String getName();

	public boolean isEqualTo(IType type);
}
