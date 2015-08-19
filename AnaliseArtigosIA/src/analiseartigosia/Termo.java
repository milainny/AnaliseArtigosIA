package analiseartigosia;


public class Termo implements Comparable<Termo>{
    private int frequencia;
    private String termo;

    public Termo() {
    }

    public Termo(int frequencia, String termo) {
        this.frequencia = frequencia;
        this.termo = termo;
    }

    public int getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(int frequencia) {
        this.frequencia = frequencia;
    }

    public String getTermo() {
        return termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    
    @Override
    public int compareTo(Termo t) {
        return Integer.compare(t.getFrequencia(),this.frequencia);
    }
    
    
}
