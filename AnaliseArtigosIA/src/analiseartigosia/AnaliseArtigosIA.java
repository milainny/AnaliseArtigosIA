/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analiseartigosia;

import analiseartigosia.areaGrafica.MenuPrincipal;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.TextPosition;
import sun.awt.FontDescriptor;

/**
 *
 * @author Lailla
 */
public class AnaliseArtigosIA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        MenuPrincipal menuPrincipal = new MenuPrincipal();
        menuPrincipal.setVisible(true);
    }

    public static String extraiTextoDoPDF(String caminho) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        String texto = stripper.getText(pdfDocument);
        pdfDocument.close();
        return texto;
    }

    public static String extraiStopWords(String textoCompleto, String caminhoArquivoStopWords) throws FileNotFoundException {
        ArrayList<String> listaStopWords = preencheLista(caminhoArquivoStopWords);
        String textoCompletoMin = textoCompleto.toLowerCase();
        textoCompletoMin = textoCompletoMin.replace("\r", "");
        textoCompletoMin = textoCompletoMin.replace("\"", "");
        textoCompletoMin = textoCompletoMin.replace("\n", " \n ");
        textoCompletoMin = textoCompletoMin.replace(".", " . ");
        textoCompletoMin = textoCompletoMin.replace("?", " ? ");
        textoCompletoMin = textoCompletoMin.replace("!", " ! ");
        textoCompletoMin = textoCompletoMin.replace(":", " : ");
        textoCompletoMin = textoCompletoMin.replace(",", " , ");
        textoCompletoMin = textoCompletoMin.replace(";", " ; ");
        textoCompletoMin = textoCompletoMin.replace("/", " / ");
        textoCompletoMin = textoCompletoMin.replace("   ", " ");
        textoCompletoMin = textoCompletoMin.replace("  ", " ");
        for (String stopWord : listaStopWords) {
            textoCompletoMin = textoCompletoMin.replace(" " + stopWord + " ", " ");
        }
        return textoCompletoMin;
    }

    public static void gravaTextoSemStopWords(String caminhoArquivoStopWords, String textoSemStopWords) throws FileNotFoundException {
        PrintStream saidaPadrao = System.out;
        PrintStream saida = new PrintStream(new FileOutputStream(new File(caminhoArquivoStopWords)));
        System.setOut(saida);
        System.out.println(textoSemStopWords);
        System.setOut(saidaPadrao);
    }

    public static String processaTermosMaisCitados(String textoSemStopWords) {
        int indexReferencia = textoSemStopWords.lastIndexOf(" reference ") != -1 ? textoSemStopWords.lastIndexOf(" reference ") : textoSemStopWords.lastIndexOf(" references ");
        String textoSemReferencias = textoSemStopWords.substring(0, indexReferencia);
        String todosTermos[] = textoSemReferencias.split(" ");
        Map<String, Integer> termos = new HashMap();
        for (String termo : todosTermos) {
            if (termos.get(termo) == null) {
                termos.put(termo, 1);
            } else {
                int valor = termos.get(termo);
                termos.put(termo, ++valor);
            }
        }
        PriorityQueue<Termo> termosOrdenados = new PriorityQueue<>();
        Set<String> chaves = termos.keySet();
        for (String chave : chaves) {
            termosOrdenados.add(new Termo(termos.get(chave), chave));
        }
        int i = 0;
        String termosString = "";
        while (i < 10) {
            Termo t = termosOrdenados.poll();
            if (t.getTermo().length() > 2) {
                termosString += "Termo: " + t.getTermo() + " - Frequencia: " + t.getFrequencia() + "\n";
                i++;
            }
        }
        return termosString;
    }

    public static String identificaInstituicoes(String textoSemStopWords, String caminhoArquivoInstituicoes) throws IOException {
        String textoPrimeirasPaginas = textoSemStopWords;
//        PrintStream saidaPadrao = System.out;
//        PrintStream saida = new PrintStream(new FileOutputStream(new File("saidas/texto2pg" + 12 + ".txt")));
//        System.setOut(saida);
//        System.out.println(textoPrimeirasPaginas);
//        System.setOut(saidaPadrao);
        String instituicoes = "";
        ArrayList<String> listaInstiuicoes = preencheLista(caminhoArquivoInstituicoes);
        for (String inst : listaInstiuicoes) {
            String textoBusca = textoPrimeirasPaginas;
            int posicaoAbs = textoPrimeirasPaginas.toLowerCase().lastIndexOf("abstract");
            if (posicaoAbs != -1) {
                textoBusca = textoPrimeirasPaginas.substring(0, textoPrimeirasPaginas.lastIndexOf("abstract"));
            }
            int posicao = textoBusca.indexOf(inst);
            if (posicao == -1) {
                if (instituicoes.equals("")) {
                    textoBusca = textoPrimeirasPaginas;
                    posicao = textoBusca.indexOf(inst);
                }
            }
            while (posicao != -1) {
                posicao = retornaPosicaoMenor(textoBusca, posicao);
                int posInicio = posicao;
                posicao = buscaFimParagrafo(textoBusca, posicao, "\n");
                instituicoes = instituicoes + textoBusca.substring(posInicio, posicao) + "\n ";
                textoBusca = textoBusca.substring(posicao, textoBusca.length());
                posicao = textoBusca.indexOf(inst);
            }
        }
        return instituicoes;
    }

    private static int buscaFimParagrafo(String textoBusca, int posicao, String operador) {
        while (true) {
            if (operador.equals(textoBusca.substring(posicao, posicao + 1))) {
                break;
            }
            posicao++;
        }
        return posicao;
    }

    private static int buscaInicioParagrafo(String textoBusca, int posicao, String operador) {
        while (true) {
            if (operador.equals(textoBusca.substring(posicao, posicao + 1))) {
                break;
            }
            posicao--;
        }
        posicao++;
        return posicao;
    }

    private static ArrayList<String> preencheLista(String caminhoArquivo) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(caminhoArquivo));
        ArrayList<String> lista = new ArrayList();
        while (scanner.hasNext()) {
            lista.add(scanner.next());
        }
        return lista;
    }

    private static ArrayList<String> preencheListaEDivide(String caminhoArquivo) throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileReader(caminhoArquivo));
        String arquivo = "";
        ArrayList<String> lista = new ArrayList();
        while (scanner.hasNext()) {
            arquivo += scanner.next() + " ";
        }
        String[] resultado = arquivo.split(" - ");
        for (String s : resultado) {
            lista.add(s.trim());
        }
        return lista;
    }

    public static String leitorDePaginas(String caminho, int quantidadePaginas) throws IOException {
        PDDocument pdfDocument = null;
        pdfDocument = PDDocument.load(caminho);
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setEndPage(quantidadePaginas);
        String textoPrimeirasPaginas = stripper.getText(pdfDocument);
        pdfDocument.close();
        return textoPrimeirasPaginas;
    }

    public static String identificaPropriedade(String caminho, String caminhoArquivoSemStopWords, String caminhoArquivoPropriedade, int qtdePaginas) throws IOException {
        String textoPaginas = "";
        if (qtdePaginas != 0) {
            textoPaginas = leitorDePaginas(caminho, qtdePaginas);
        } else {
            textoPaginas = extraiTextoDoPDF(caminho);
        }
        String textoSemStopWords = extraiStopWords(textoPaginas, caminhoArquivoSemStopWords);
        textoPaginas = textoSemStopWords.toLowerCase();
        ArrayList<String> listaMetaDadosPropriedade = preencheListaEDivide(caminhoArquivoPropriedade);
        ArrayList<String> naoRepetir = new ArrayList<>();

        String resultado = "";
        for (String obj : listaMetaDadosPropriedade) {
            int posicao = textoSemStopWords.indexOf(obj);
            if (posicao != -1) {
                posicao = retornaPosicaoMenor(textoSemStopWords, posicao);
                int posInicio = posicao;
                posicao = buscaFimParagrafo(textoSemStopWords, posicao, ".");
                resultado = textoSemStopWords.substring(posInicio, posicao);
                if (!naoRepetir.contains(resultado)) {
                    naoRepetir.add(resultado);
                }
            }
        }
        String objetivoPropriedade = "";
        for (String s : naoRepetir) {
            objetivoPropriedade += s + "\n";
        }
        return objetivoPropriedade;

    }

    public static String extraiReferencias(String texto, String caminhoReferencias) throws FileNotFoundException {
        String textoReferencias = texto.replace("\r", "");
        int indexReferences = textoReferencias.lastIndexOf("References\n");
        if (indexReferences == -1) {
            indexReferences = textoReferencias.lastIndexOf("Reference\n");
        }
        textoReferencias = textoReferencias.substring(indexReferences, textoReferencias.length());
        ArrayList<String> listaMetaDadosFimReferencia = preencheListaEDivide(caminhoReferencias);        
        for (String metaDado : listaMetaDadosFimReferencia) {
            int indexMetaDado = textoReferencias.lastIndexOf(metaDado);
            if (indexMetaDado != -1) {
                textoReferencias = textoReferencias.substring(0, indexMetaDado);
                break;
            }
        }
        return textoReferencias;
    }

    private static int retornaPosicaoMenor(String textoSemStopWords, int posicao) {
        int pos1, pos2;
        pos1 = buscaInicioParagrafo(textoSemStopWords, posicao, ".");
        pos2 = buscaInicioParagrafo(textoSemStopWords, posicao, "\n");
        if (pos2 > pos1) {
            return pos2;
        }
        return pos1;

    }

}
