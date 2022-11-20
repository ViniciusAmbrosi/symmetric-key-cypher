# symmetric-key-cypher

# Membros do grupo
Vinicius Ambrosi

# Descrição
Esse trabalho inclui um cifrador simétrico de bloco (para blocos de 48 bits), de forma a aplicar e demonstrar a criptografa e decriptografia utilizando uma chave de 4 bytes / 32 bits. A chave pode ou não ser fornecida pelo usuário, caso omitida uma interna será utilizada. O modo de operação do cifrador segue o modelo de Cipher Block Chaining (ou CBC), onde o resultado de cada bloco serve como entrada para a criptografia / decriptografia do próximo block. Como na imagem a baixo:
![alt text](https://media.geeksforgeeks.org/wp-content/uploads/Cipher-Block-Chaining-1.png)

# Classes principais

## CipherBlockChainHandle
Responsável por processar o modelo de CBC do cifrador, aplicado um xor do bloco corrente e do último bloco. O vetor de inicialização é gerado de forma altearóia no SymmetricCipher. O inicializador gera uma matriz de 48 bits e o valor de cada posição é obtido aleatoriamente para cada execução do cifrador.

## CipherKeyScheduler
Responsável por orquestrar e gerenciar as chaves utilizadas. São utilizadas oito sub-chaves geradas a partir da chave fornecida pelo usuário. Para gerar as sub chaves são aplicadas permutações na chave base e uma para cada sub chave. A forma que as permutações são aplicadas é randomico, visto que as matrizes de permutação são geradas de forma aleatória para cada execução do algorítmo. Em resumo, o processo de geração das chaves segue:
* Permutar a chave base
* Gerar 8 sub chaves para a chave permutada, cujo processo é:
   * Consome chave anterior (base ou última sub-chave gerada)
   * Separa a chave em duas (esquerda e direita)
   * Rotaciona 2 bits para a esquerda para ambas partes
   * Junta as duas partes aplicando um shift the 16 bits para esquerda
   * Aplica permutação da sub-chave baseado na matriz de permutação de sub chaves
   * Salva a chave

Como é feito o encadeamento das sub chaves, na decriptografia é aplicado na ordem inversa.

## CipherPaddingHandler
Responsável por adicionar/ler o header relacionado a quantidade de bytes/bits adicionados para o padding. O processamento é feito antes da criptografia/decriptografia do atigo baseado nos bits de entrada do algoritmo. Desta forma, na criptografia os bits são contados e determina-se quantos bits de padding serão necessários no último bloco. Na decriptografia, é feita a leitura do header sobre bits de padding e são retirados esses bits no final do processo de deciframento.

## SymmetricCipher
Responsável por processar o ciframento e o deciframento do conteúdo do arquivo.

Para criptografia:
* Faz a leitura de todos os blocos de mensagem (de 48 bits) proativamente
* Aplica Cipher Block Chaining from o bloco de mensagem e (bloco anterior ou vetor de inicialização)
* Aplica substituição e transposição de cada bit por todas as sub chaves geradas (ordem crescente)
* Salva resultado no arquivo encryptResult

Para decriptografia:
* Faz a leitura de todos os blocos de mensagem (de 48 bits) proativamente
* Aplica Cipher Block Chaining from o bloco de mensagem e (bloco anterior ou vetor de inicialização)
* Aplica substituição e transposição de cada bit por todas as sub chaves geradas (ordem decrescente)
* Salva resultado no arquivo decryptResult

# Desenvolvimento
O trabalho foi desenvolvido em Java e Maven, sendo estes uma depêndencia para a execução do projeto.

# Execução
De forma a executar o projeto é necessario:
* Clonar o projeto
* Ir ao diretório do projeto no terminal
* Rodar mvn clean install package 
   * Clean - limpar targets
   * Install - instala dependências do gerenciador maven
   * Package - gera .jar na pasta targets para execução
* Pode optar agora por rodar, utilizando:
   * O jar manualmente com 'java -jar "nome_do_jar.jar" <caminho_do_arquivo> <chave_de_ciframento>' localizado na pasta target OU
   * Os targets to maven com 'mvn exec:java -Dexec.mainClass=App "-Dexec.args=<<caminho_do_arquivo> <chave_de_ciframento>".
* Executar um dos comandos acima, sendo que os argumentos são baseados em:
   * caminho_do_arquivo é o caminho absoluto de um arquivo txt
   * chave_de_ciframento é uma string de no máximo 4 bytes / 32 bits (4 caractéres)
* Deve ver ser gerado na pasta resources um arquivo encryptResult
* Deve ver ser gerado na pasta resources um arquivo decryptResult
