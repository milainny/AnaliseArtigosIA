/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package analiseartigosia;

import analiseartigosia.areaGrafica.MenuPrincipal;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;
import javax.swing.JFrame;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

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
        menuPrincipal.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
                termosString += i+1 + ") " + t.getTermo() + " - freq.: " + t.getFrequencia() + " \n";
                i++;
            }
        }
        return termosString;
    }

    public static String identificaInstituicoesEAutores(String texto, String caminhoArquivoInstituicoes) throws IOException {
        String textoPrimeirasPaginas = texto;
        String instituicoes = "";
        textoPrimeirasPaginas = textoPrimeirasPaginas.replace("\r", "");
        ArrayList<String> listaInstiuicoes = preencheLista(caminhoArquivoInstituicoes);
        ArrayList<String> naoRepetir = new ArrayList<>();
        for (String inst : listaInstiuicoes) {
            String textoBusca = textoPrimeirasPaginas;
            int posicaoAbs = textoPrimeirasPaginas.lastIndexOf("Abstract\n");
            if (posicaoAbs != -1) {
                textoBusca = textoPrimeirasPaginas.substring(0, textoPrimeirasPaginas.lastIndexOf("Abstract\n"));
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
                instituicoes = textoBusca.substring(posInicio, posicao);
                if (!naoRepetir.contains(instituicoes)) {
                    naoRepetir.add(instituicoes);
                }
                textoBusca = textoBusca.substring(posicao, textoBusca.length());
                posicao = textoBusca.indexOf(inst);
            }
        }
        instituicoes = "";
        for (String s : naoRepetir) {
            instituicoes += s + "\n";
        }
        String autores = textoPrimeirasPaginas.substring(0, textoPrimeirasPaginas.indexOf(instituicoes.substring(0, instituicoes.indexOf("\n"))));
        autores = autores.substring(0, autores.lastIndexOf("\n"));
        autores = autores.substring(autores.lastIndexOf("\n"), autores.length());
        return autores + " / " + instituicoes;
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

    public static String identificaPropriedade(String caminho, String caminhoArquivoPropriedade, int qtdePaginas) throws IOException {
        String textoPaginas = "";
        if (qtdePaginas != 0) {
            textoPaginas = leitorDePaginas(caminho, qtdePaginas);
        } else {
            textoPaginas = extraiTextoDoPDF(caminho);
        }
        textoPaginas = textoPaginas.toLowerCase().replace("\r", "");
        ArrayList<String> listaMetaDadosPropriedade = preencheListaEDivide(caminhoArquivoPropriedade);
        ArrayList<String> naoRepetir = new ArrayList<>();

        String resultado = "";
        for (String obj : listaMetaDadosPropriedade) {
            int posicao = textoPaginas.indexOf(obj);
            if (posicao != -1) {
                posicao = retornaPosicaoMenor(textoPaginas, posicao);
                int posInicio = posicao;
                posicao = buscaFimParagrafo(textoPaginas, posicao, ".");
                resultado = textoPaginas.substring(posInicio, posicao);
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

    private static int retornaPosicaoMenor(String texto, int posicao) {
        int pos1, pos2;
        try {
            pos1 = buscaInicioParagrafo(texto, posicao, ".");
        } catch (Exception e) {
            pos1 = -1;
        }
        try {
            pos2 = buscaInicioParagrafo(texto, posicao, "\n");
        } catch (Exception e) {
            pos2 = -1;
        }
        if (pos2 > pos1) {
            return pos2;
        }
        return pos1;

    }

}
