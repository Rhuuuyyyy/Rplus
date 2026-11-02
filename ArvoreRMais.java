import java.util.Scanner;

public class ArvoreRMais {

    static final int MAX_ENTRADAS = 4;
    static final int CAPACIDADE = MAX_ENTRADAS + 1;
    static final int LIMITE_LUGARES = 100;
    static final int TAMANHO_MAPA = 1000;
    static final int TAMANHO_PILHA = 1000;

    static class No {
        boolean folha;
        int quantidade;
        int[] regiao = new int[4];
        int[][] coord = new int[4][CAPACIDADE];
        int[] indiceLugar = new int[CAPACIDADE];
        No[] filhos = new No[CAPACIDADE];
        No pai;

        No(boolean folha) {
            this.folha = folha;
        }
    }

    static No raiz;
    static int altura;
    static boolean mostrarMensagens = true;

    static int[] lugarId = new int[LIMITE_LUGARES];
    static String[] lugarNome = new String[LIMITE_LUGARES];
    static int[][] lugarRet = new int[4][LIMITE_LUGARES];
    static boolean[] lugarAtivo = new boolean[LIMITE_LUGARES];
    static int totalLugares;

    static int[] resultado = new int[LIMITE_LUGARES];
    static int totalResultado;
    static int nosVisitados;

    static No[] folhasEncontradas = new No[TAMANHO_PILHA];
    static int totalFolhasEncontradas;

    static Scanner teclado = new Scanner(System.in);

    static void iniciarArvore() {
        raiz = new No(true);
        raiz.regiao[2] = TAMANHO_MAPA;
        raiz.regiao[3] = TAMANHO_MAPA;
        altura = 1;
        totalLugares = 0;
        for (int i = 0; i < LIMITE_LUGARES; i++) {
            lugarAtivo[i] = false;
        }
    }

    static String formatarRetangulo(int xMin, int yMin, int xMax, int yMax) {
        return "(" + xMin + "," + yMin + ")-(" + xMax + "," + yMax + ")";
    }

    static String formatarRegiao(No no) {
        return formatarRetangulo(no.regiao[0], no.regiao[1], no.regiao[2], no.regiao[3]);
    }

    static boolean entradaCruza(No no, int pos, int xMin, int yMin, int xMax, int yMax) {
        return no.coord[0][pos] <= xMax && no.coord[2][pos] >= xMin
                && no.coord[1][pos] <= yMax && no.coord[3][pos] >= yMin;
    }

    static void copiarEntrada(No origem, int posOrigem, No destino, int posDestino) {
        for (int k = 0; k < 4; k++) {
            destino.coord[k][posDestino] = origem.coord[k][posOrigem];
        }
        destino.indiceLugar[posDestino] = origem.indiceLugar[posOrigem];
        destino.filhos[posDestino] = origem.filhos[posOrigem];
    }

    static void gravarRegiaoDoFilho(No pai, int pos, No filho) {
        for (int k = 0; k < 4; k++) {
            pai.coord[k][pos] = filho.regiao[k];
        }
        pai.filhos[pos] = filho;
    }

    static int posicaoDoFilho(No pai, No filho) {
        int pos = 0;
        while (pai.filhos[pos] != filho) {
            pos++;
        }
        return pos;
    }

    static void removerEntrada(No no, int pos) {
        for (int i = pos; i < no.quantidade - 1; i++) {
            copiarEntrada(no, i + 1, no, i);
        }
        no.quantidade--;
    }

    static int acharPorId(int id) {
        for (int i = 0; i < LIMITE_LUGARES; i++) {
            if (lugarAtivo[i] && lugarId[i] == id) {
                return i;
            }
        }
        return -1;
    }

    static void coletarFolhas(int xMin, int yMin, int xMax, int yMax) {
        totalFolhasEncontradas = 0;
        nosVisitados = 0;
        No[] pilha = new No[TAMANHO_PILHA];
        int topo = 1;
        pilha[0] = raiz;
        while (topo > 0) {
            topo--;
            No atual = pilha[topo];
            nosVisitados++;
            if (atual.folha) {
                folhasEncontradas[totalFolhasEncontradas] = atual;
                totalFolhasEncontradas++;
            } else {
                for (int i = 0; i < atual.quantidade; i++) {
                    if (entradaCruza(atual, i, xMin, yMin, xMax, yMax)) {
                        pilha[topo] = atual.filhos[i];
                        topo++;
                    }
                }
            }
        }
    }

    static int inserir(int id, String nome, int xMin, int yMin, int xMax, int yMax) {
        if (id <= 0 || xMin < 0 || yMin < 0 || xMax > TAMANHO_MAPA || yMax > TAMANHO_MAPA
                || xMin > xMax || yMin > yMax) {
            return 2;
        }
        if (acharPorId(id) != -1) {
            return 1;
        }
        int posLivre = 0;
        while (posLivre < LIMITE_LUGARES && lugarAtivo[posLivre]) {
            posLivre++;
        }
        if (posLivre == LIMITE_LUGARES) {
            return 3;
        }

        int[] novoRet = {xMin, yMin, xMax, yMax};
        int[][] candidatos = new int[2][LIMITE_LUGARES + 1];
        int[] qtdCandidatos = {1, 1};
        candidatos[0][0] = xMin;
        candidatos[1][0] = yMin;
        for (int j = 0; j < LIMITE_LUGARES; j++) {
            for (int eixo = 0; eixo < 2 && lugarAtivo[j]; eixo++) {
                if (lugarRet[eixo][j] > novoRet[eixo] && lugarRet[eixo][j] <= novoRet[eixo + 2]) {
                    candidatos[eixo][qtdCandidatos[eixo]] = lugarRet[eixo][j];
                    qtdCandidatos[eixo]++;
                }
            }
        }
        for (int cx = 0; cx < qtdCandidatos[0]; cx++) {
            for (int cy = 0; cy < qtdCandidatos[1]; cy++) {
                int lugaresNaCelula = 0;
                for (int j = 0; j < LIMITE_LUGARES; j++) {
                    if (lugarAtivo[j] && lugarRet[0][j] <= candidatos[0][cx] && lugarRet[2][j] >= candidatos[0][cx]
                            && lugarRet[1][j] <= candidatos[1][cy] && lugarRet[3][j] >= candidatos[1][cy]) {
                        lugaresNaCelula++;
                    }
                }
                if (lugaresNaCelula >= MAX_ENTRADAS) {
                    return 4;
                }
            }
        }

        lugarId[posLivre] = id;
        lugarNome[posLivre] = nome;
        for (int k = 0; k < 4; k++) {
            lugarRet[k][posLivre] = novoRet[k];
        }
        lugarAtivo[posLivre] = true;
        totalLugares++;

        coletarFolhas(xMin, yMin, xMax, yMax);
        for (int f = 0; f < totalFolhasEncontradas; f++) {
            No folhaAtual = folhasEncontradas[f];
            for (int k = 0; k < 4; k++) {
                folhaAtual.coord[k][folhaAtual.quantidade] = novoRet[k];
            }
            folhaAtual.indiceLugar[folhaAtual.quantidade] = posLivre;
            folhaAtual.quantidade++;
        }

        No noCheio = acharNoCheio();
        while (noCheio != null) {
            dividir(noCheio);
            noCheio = acharNoCheio();
        }
        return 0;
    }

    static No acharNoCheio() {
        No[] pilha = new No[TAMANHO_PILHA];
        int topo = 1;
        pilha[0] = raiz;
        while (topo > 0) {
            topo--;
            No atual = pilha[topo];
            if (atual.quantidade > MAX_ENTRADAS) {
                return atual;
            }
            if (!atual.folha) {
                for (int i = 0; i < atual.quantidade; i++) {
                    pilha[topo] = atual.filhos[i];
                    topo++;
                }
            }
        }
        return null;
    }

    static No criarIrmao(No original, int eixo, int corte, No pai) {
        No irmao = new No(original.folha);
        irmao.pai = pai;
        for (int k = 0; k < 4; k++) {
            irmao.regiao[k] = original.regiao[k];
        }
        original.regiao[eixo + 2] = corte;
        irmao.regiao[eixo] = corte + 1;
        return irmao;
    }

    static void dividir(No no) {
        int[] melhorCorte = new int[2];
        int[] melhorCusto = new int[2];
        int[] melhorDiferenca = new int[2];
        boolean[] achouCorte = new boolean[2];
        for (int eixoTestado = 0; eixoTestado < 2; eixoTestado++) {
            for (int i = 0; i < no.quantidade; i++) {
                for (int lado = 0; lado < 2; lado++) {
                    int posCorte = no.coord[eixoTestado][i] - 1;
                    if (lado == 0) {
                        posCorte = no.coord[eixoTestado + 2][i];
                    }
                    if (posCorte >= no.regiao[eixoTestado] && posCorte < no.regiao[eixoTestado + 2]) {
                        int contEsq = 0;
                        int contDir = 0;
                        for (int j = 0; j < no.quantidade; j++) {
                            if (no.coord[eixoTestado][j] <= posCorte) {
                                contEsq++;
                            }
                            if (no.coord[eixoTestado + 2][j] > posCorte) {
                                contDir++;
                            }
                        }
                        if (contEsq <= MAX_ENTRADAS && contDir <= MAX_ENTRADAS) {
                            int custo = contEsq + contDir - no.quantidade;
                            int diferenca = contEsq - contDir;
                            if (diferenca < 0) {
                                diferenca = -diferenca;
                            }
                            if (!achouCorte[eixoTestado] || diferenca < melhorDiferenca[eixoTestado]
                                    || (diferenca == melhorDiferenca[eixoTestado] && custo < melhorCusto[eixoTestado])) {
                                achouCorte[eixoTestado] = true;
                                melhorCorte[eixoTestado] = posCorte;
                                melhorCusto[eixoTestado] = custo;
                                melhorDiferenca[eixoTestado] = diferenca;
                            }
                        }
                    }
                }
            }
        }
        int eixoDoCorte = 0;
        if (!achouCorte[0] || (achouCorte[1] && melhorCusto[1] < melhorCusto[0])
                || (achouCorte[1] && melhorCusto[1] == melhorCusto[0] && melhorDiferenca[1] < melhorDiferenca[0])) {
            eixoDoCorte = 1;
        }
        int corte = melhorCorte[eixoDoCorte];

        No novoNo = criarIrmao(no, eixoDoCorte, corte, no.pai);
        if (mostrarMensagens) {
            System.out.println("Divisao de " + nomeTipo(no.folha) + ": corte " + nomeEixo(eixoDoCorte) + "=" + corte
                    + ": " + formatarRegiao(no) + " e " + formatarRegiao(novoNo));
        }

        No[] pilhaEsq = new No[TAMANHO_PILHA];
        No[] pilhaDir = new No[TAMANHO_PILHA];
        pilhaEsq[0] = no;
        pilhaDir[0] = novoNo;
        int topo = 1;
        while (topo > 0) {
            topo--;
            No noEsq = pilhaEsq[topo];
            No noDir = pilhaDir[topo];
            int qtdEsq = 0;
            int qtdDir = 0;
            int total = noEsq.quantidade;
            for (int i = 0; i < total; i++) {
                boolean vaiEsq = noEsq.coord[eixoDoCorte][i] <= corte;
                boolean vaiDir = noEsq.coord[eixoDoCorte + 2][i] > corte;
                if (vaiEsq && vaiDir && !noEsq.folha) {
                    No filhoEsq = noEsq.filhos[i];
                    No filhoDir = criarIrmao(filhoEsq, eixoDoCorte, corte, noDir);
                    gravarRegiaoDoFilho(noEsq, qtdEsq, filhoEsq);
                    qtdEsq++;
                    gravarRegiaoDoFilho(noDir, qtdDir, filhoDir);
                    qtdDir++;
                    pilhaEsq[topo] = filhoEsq;
                    pilhaDir[topo] = filhoDir;
                    topo++;
                    if (mostrarMensagens) {
                        System.out.println("  corte forcado em " + nomeTipo(filhoEsq.folha) + " "
                                + formatarRegiao(filhoEsq) + " e " + formatarRegiao(filhoDir));
                    }
                } else {
                    if (vaiEsq) {
                        copiarEntrada(noEsq, i, noEsq, qtdEsq);
                        qtdEsq++;
                    }
                    if (vaiDir) {
                        copiarEntrada(noEsq, i, noDir, qtdDir);
                        if (!noDir.folha) {
                            noDir.filhos[qtdDir].pai = noDir;
                        }
                        qtdDir++;
                    }
                }
            }
            noEsq.quantidade = qtdEsq;
            noDir.quantidade = qtdDir;
        }

        if (no.pai == null) {
            No novaRaiz = new No(false);
            novaRaiz.regiao[2] = TAMANHO_MAPA;
            novaRaiz.regiao[3] = TAMANHO_MAPA;
            gravarRegiaoDoFilho(novaRaiz, 0, no);
            gravarRegiaoDoFilho(novaRaiz, 1, novoNo);
            novaRaiz.quantidade = 2;
            no.pai = novaRaiz;
            novoNo.pai = novaRaiz;
            raiz = novaRaiz;
            altura++;
            if (mostrarMensagens) {
                System.out.println("Raiz dividida: a arvore cresceu, altura = " + altura);
            }
        } else {
            No pai = no.pai;
            gravarRegiaoDoFilho(pai, posicaoDoFilho(pai, no), no);
            gravarRegiaoDoFilho(pai, pai.quantidade, novoNo);
            pai.quantidade++;
        }
    }

    static String nomeTipo(boolean folha) {
        if (folha) {
            return "FOLHA";
        }
        return "INTERNO";
    }

    static String nomeEixo(int eixo) {
        if (eixo == 0) {
            return "vertical x";
        }
        return "horizontal y";
    }

    static void buscarJanela(int xMin, int yMin, int xMax, int yMax) {
        coletarFolhas(xMin, yMin, xMax, yMax);
        totalResultado = 0;
        for (int f = 0; f < totalFolhasEncontradas; f++) {
            No folhaAtual = folhasEncontradas[f];
            for (int i = 0; i < folhaAtual.quantidade; i++) {
                boolean jaTem = false;
                for (int j = 0; j < totalResultado; j++) {
                    if (resultado[j] == folhaAtual.indiceLugar[i]) {
                        jaTem = true;
                    }
                }
                if (entradaCruza(folhaAtual, i, xMin, yMin, xMax, yMax) && !jaTem) {
                    resultado[totalResultado] = folhaAtual.indiceLugar[i];
                    totalResultado++;
                }
            }
        }
    }

    static void buscarPonto(int x, int y) {
        buscarJanela(x, y, x, y);
    }

    static boolean remover(int id) {
        int indice = acharPorId(id);
        if (indice == -1) {
            return false;
        }

        No[] folhasVazias = new No[TAMANHO_PILHA];
        int qtdVazias = 0;
        coletarFolhas(lugarRet[0][indice], lugarRet[1][indice], lugarRet[2][indice], lugarRet[3][indice]);
        for (int f = 0; f < totalFolhasEncontradas; f++) {
            No folhaAtual = folhasEncontradas[f];
            int pos = -1;
            for (int i = 0; i < folhaAtual.quantidade; i++) {
                if (folhaAtual.indiceLugar[i] == indice) {
                    pos = i;
                }
            }
            if (pos != -1) {
                removerEntrada(folhaAtual, pos);
                if (folhaAtual.quantidade == 0 && folhaAtual != raiz) {
                    folhasVazias[qtdVazias] = folhaAtual;
                    qtdVazias++;
                }
            }
        }

        for (int v = 0; v < qtdVazias; v++) {
            No folhaVazia = folhasVazias[v];
            No pai = folhaVazia.pai;
            int posIrma = -1;
            for (int i = 0; i < pai.quantidade; i++) {
                No candidata = pai.filhos[i];
                if (candidata != folhaVazia && candidata.folha && posIrma == -1) {
                    boolean mesmaAltura = candidata.regiao[1] == folhaVazia.regiao[1]
                            && candidata.regiao[3] == folhaVazia.regiao[3];
                    boolean mesmaLargura = candidata.regiao[0] == folhaVazia.regiao[0]
                            && candidata.regiao[2] == folhaVazia.regiao[2];
                    boolean encostaX = candidata.regiao[2] + 1 == folhaVazia.regiao[0]
                            || folhaVazia.regiao[2] + 1 == candidata.regiao[0];
                    boolean encostaY = candidata.regiao[3] + 1 == folhaVazia.regiao[1]
                            || folhaVazia.regiao[3] + 1 == candidata.regiao[1];
                    if ((mesmaAltura && encostaX) || (mesmaLargura && encostaY)) {
                        posIrma = i;
                    }
                }
            }
            if (posIrma != -1) {
                No irma = pai.filhos[posIrma];
                if (mostrarMensagens) {
                    System.out.println("Juncao: folha vazia " + formatarRegiao(folhaVazia)
                            + " absorvida pela irma " + formatarRegiao(irma));
                }
                for (int k = 0; k < 2; k++) {
                    if (folhaVazia.regiao[k] < irma.regiao[k]) {
                        irma.regiao[k] = folhaVazia.regiao[k];
                    }
                    if (folhaVazia.regiao[k + 2] > irma.regiao[k + 2]) {
                        irma.regiao[k + 2] = folhaVazia.regiao[k + 2];
                    }
                }
                gravarRegiaoDoFilho(pai, posIrma, irma);
                removerEntrada(pai, posicaoDoFilho(pai, folhaVazia));
            }
        }

        while (!raiz.folha && raiz.quantidade == 1) {
            raiz = raiz.filhos[0];
            raiz.pai = null;
            altura--;
            if (mostrarMensagens) {
                System.out.println("Raiz com um filho so: a arvore encolheu, altura = " + altura);
            }
        }

        lugarAtivo[indice] = false;
        totalLugares--;
        return true;
    }

    static int contarCopias(int indice) {
        int total = 0;
        coletarFolhas(lugarRet[0][indice], lugarRet[1][indice], lugarRet[2][indice], lugarRet[3][indice]);
        for (int f = 0; f < totalFolhasEncontradas; f++) {
            for (int i = 0; i < folhasEncontradas[f].quantidade; i++) {
                if (folhasEncontradas[f].indiceLugar[i] == indice) {
                    total++;
                }
            }
        }
        return total;
    }

    static void imprimirArvore() {
        No[] pilha = new No[TAMANHO_PILHA];
        int[] nivel = new int[TAMANHO_PILHA];
        int topo = 1;
        pilha[0] = raiz;
        nivel[0] = 0;
        int totalNos = 0;
        int totalFolhas = 0;
        int totalCopias = 0;
        while (topo > 0) {
            topo--;
            No atual = pilha[topo];
            int nivelAtual = nivel[topo];
            totalNos++;
            String linha = "";
            for (int k = 0; k < nivelAtual; k++) {
                linha = linha + "    ";
            }
            linha = linha + nomeTipo(atual.folha) + " " + formatarRegiao(atual) + " [" + atual.quantidade + "]:";
            if (atual.folha) {
                totalFolhas++;
                for (int i = 0; i < atual.quantidade; i++) {
                    int lugar = atual.indiceLugar[i];
                    linha = linha + " " + lugarId[lugar] + "-" + lugarNome[lugar];
                    totalCopias++;
                    if (lugarRet[0][lugar] < atual.regiao[0] || lugarRet[1][lugar] < atual.regiao[1]
                            || lugarRet[2][lugar] > atual.regiao[2] || lugarRet[3][lugar] > atual.regiao[3]) {
                        linha = linha + "*";
                    }
                }
            } else {
                for (int i = 0; i < atual.quantidade; i++) {
                    linha = linha + " " + formatarRetangulo(atual.coord[0][i], atual.coord[1][i],
                            atual.coord[2][i], atual.coord[3][i]);
                }
                for (int i = atual.quantidade - 1; i >= 0; i--) {
                    pilha[topo] = atual.filhos[i];
                    nivel[topo] = nivelAtual + 1;
                    topo++;
                }
            }
            System.out.println(linha);
        }
        System.out.println("Altura " + altura + ", " + totalNos + " nos, " + totalFolhas + " folhas, "
                + totalLugares + " lugares, " + totalCopias
                + " referencias nas folhas (* = lugar cruza a divisa e aparece em mais de uma folha)");
    }

    static void imprimirResultado() {
        for (int i = 0; i < totalResultado; i++) {
            int lugar = resultado[i];
            System.out.println("  " + lugarId[lugar] + " " + lugarNome[lugar] + " "
                    + formatarRetangulo(lugarRet[0][lugar], lugarRet[1][lugar], lugarRet[2][lugar], lugarRet[3][lugar]));
        }
        if (totalResultado == 0) {
            System.out.println("  nenhum lugar encontrado");
        }
        System.out.println("Nos visitados: " + nosVisitados);
    }

    static void carregarExemplo() {
        iniciarArvore();
        String[] nomes = {"PUCPR_Campus", "Jardim_Botanico", "Parque_Barigui", "Parque_Tangua", "Arena_da_Baixada",
            "Couto_Pereira", "Rodoferroviaria", "Terminal_Boqueirao", "Passeio_Publico", "Opera_de_Arame",
            "Bairro_Centro", "Bairro_Batel", "Bairro_Santa_Felicidade", "Bairro_Cajuru", "Linha_Verde"};
        int[][] retangulos = {
            {420, 120, 520, 210}, {480, 380, 560, 450}, {150, 450, 260, 650}, {520, 780, 600, 880},
            {330, 400, 400, 470}, {640, 600, 710, 670}, {400, 330, 450, 380}, {600, 100, 660, 170},
            {450, 500, 500, 550}, {560, 700, 620, 760}, {380, 460, 620, 620}, {250, 380, 420, 520},
            {60, 600, 330, 900}, {650, 400, 900, 700}, {500, 0, 560, 1000}};
        for (int i = 0; i < 15; i++) {
            inserir(i + 1, nomes[i], retangulos[i][0], retangulos[i][1], retangulos[i][2], retangulos[i][3]);
        }
        System.out.println("15 lugares carregados.");
    }

    static int lerInt(String mensagem) {
        System.out.print(mensagem);
        while (!teclado.hasNextInt()) {
            teclado.next();
            System.out.print("Valor invalido, digite um numero inteiro: ");
        }
        return teclado.nextInt();
    }

    static int[] lerRetangulo() {
        int[] valores = new int[4];
        valores[0] = lerInt("xmin: ");
        valores[1] = lerInt("ymin: ");
        valores[2] = lerInt("xmax: ");
        valores[3] = lerInt("ymax: ");
        return valores;
    }

    public static void main(String[] args) {
        iniciarArvore();
        int opcao = -1;
        while (opcao != 0) {
            System.out.println();
            System.out.println("R+-Tree - lugares de Curitiba (grade esquematica 0..1000)");
            System.out.println("1 - Inserir lugar");
            System.out.println("2 - Buscar ponto (lugar que contem o ponto)");
            System.out.println("3 - Buscar area (lugares dentro de uma janela)");
            System.out.println("4 - Remover lugar por id");
            System.out.println("5 - Imprimir arvore");
            System.out.println("6 - Carregar dados de exemplo");
            System.out.println("7 - Ligar/desligar mensagens de divisao");
            System.out.println("0 - Sair");
            opcao = lerInt("Opcao: ");

            if (opcao == 1) {
                int id = lerInt("Id: ");
                System.out.print("Nome (sem espacos): ");
                String nome = teclado.next();
                int[] ret = lerRetangulo();
                int codigo = inserir(id, nome, ret[0], ret[1], ret[2], ret[3]);
                if (codigo == 0) {
                    System.out.println("Inserido. Referencias nas folhas: " + contarCopias(acharPorId(id)));
                } else if (codigo == 1) {
                    System.out.println("Ja existe um lugar com esse id.");
                } else if (codigo == 2) {
                    System.out.println("Dados invalidos: id maior que 0, coordenadas de 0 a " + TAMANHO_MAPA
                            + ", min <= max.");
                } else if (codigo == 3) {
                    System.out.println("Tabela de lugares cheia (" + LIMITE_LUGARES + ").");
                } else {
                    System.out.println("Rejeitado: " + (MAX_ENTRADAS + 1)
                            + " lugares cobririam a mesma celula e nenhum corte consegue separar "
                            + "(limitacao da R+-tree: no maximo " + MAX_ENTRADAS + " objetos por celula).");
                }
            } else if (opcao == 2) {
                int x = lerInt("x: ");
                int y = lerInt("y: ");
                System.out.println("Lugares que contem o ponto (" + x + "," + y + "):");
                buscarPonto(x, y);
                imprimirResultado();
            } else if (opcao == 3) {
                int[] ret = lerRetangulo();
                if (ret[0] > ret[2] || ret[1] > ret[3]) {
                    System.out.println("Janela invalida: min deve ser <= max.");
                } else {
                    System.out.println("Lugares dentro da janela "
                            + formatarRetangulo(ret[0], ret[1], ret[2], ret[3]) + ":");
                    buscarJanela(ret[0], ret[1], ret[2], ret[3]);
                    imprimirResultado();
                }
            } else if (opcao == 4) {
                int id = lerInt("Id para remover: ");
                if (remover(id)) {
                    System.out.println("Lugar " + id + " removido de todas as folhas.");
                } else {
                    System.out.println("Nao existe lugar com esse id.");
                }
            } else if (opcao == 5) {
                imprimirArvore();
            } else if (opcao == 6) {
                carregarExemplo();
            } else if (opcao == 7) {
                mostrarMensagens = !mostrarMensagens;
                if (mostrarMensagens) {
                    System.out.println("Mensagens de divisao ligadas.");
                } else {
                    System.out.println("Mensagens de divisao desligadas.");
                }
            } else if (opcao != 0) {
                System.out.println("Opcao invalida.");
            }
        }
        System.out.println("Fim.");
    }
}
