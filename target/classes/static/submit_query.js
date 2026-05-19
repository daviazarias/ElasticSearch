const maxPageButtons = 10;

const pageButtons =
    document.getElementById("pagination");

const resultsList =
    document.getElementById("results");

let currentPage = 1;

/* =========================
   ENVIO DA PESQUISA
========================= */

function sendQuery(page) {

    const query =
        document.getElementById('query').value;

    const queryURL =
        "query=" + encodeURIComponent(query);

    const pageURL =
        page ? ("&page=" + page) : "";

    const url =
        document.URL + "search?" + queryURL + pageURL;

    fetch(url)

        .then(response => {

            if (!response.ok) {
                throw new Error('Não foi ok :(');
            }

            return response.json();
        })

        .then(data => showResults(data))

        .catch(error =>
            console.error('Fetch error:', error)
        );
}

/* =========================
   ENTER PARA PESQUISAR
========================= */

const input =
    document.querySelector("input");

input.addEventListener("keydown", (event) => {

    if (event.key === "Enter") {

        event.preventDefault();

        currentPage = 1;

        sendQuery(null);
    }
});

/* =========================
   MOSTRAR RESULTADOS
========================= */

function showResults(data) {

    resultsList.replaceChildren();

    displayPageButtons(data.numeroPaginas);

    for (let result of data.results) {

        /* Título */

        const tituloContainer =
            document.createElement("dt");

        const link =
            document.createElement("a");

        link.textContent = result.title;

        link.href = result.url;

        link.target = "_blank";

        tituloContainer.appendChild(link);

        /* Resumo */

        const abstractContainer =
            document.createElement("dd");

        abstractContainer.textContent =
            result.abs;

        /* Adiciona */

        resultsList.appendChild(tituloContainer);

        resultsList.appendChild(abstractContainer);
    }
}

/* =========================
   PAGINAÇÃO
========================= */

function displayPageButtons(totalPages) {

    pageButtons.replaceChildren();

    const lesserHalfPageNumber =
        currentPage - Math.floor(maxPageButtons / 2);

    const initialButton =
        (lesserHalfPageNumber > 0)
            ? lesserHalfPageNumber
            : 1;

    const pagesAmount =
        Math.min(
            maxPageButtons,
            totalPages - initialButton + 1
        );

    /* Botão anterior */

    if (currentPage !== 1) {

        const previousLi =
            document.createElement("li");

        const previousButton =
            document.createElement("button");

        previousButton.textContent =
            "« Anterior";

        previousButton.dataset.action =
            "prev";

        previousLi.appendChild(previousButton);

        pageButtons.appendChild(previousLi);
    }

    /* Botões numéricos */

    for (let i = 0; i < pagesAmount; i++) {

        const pageNumber =
            initialButton + i;

        const pageButtonContainer =
            document.createElement("li");

        const button =
            document.createElement("button");

        button.textContent = pageNumber;

        /* Página atual */

        if (pageNumber == currentPage) {
            button.classList.add("active");
        }

        pageButtonContainer.appendChild(button);

        pageButtons.appendChild(pageButtonContainer);
    }

    /* Botão próximo */

    if (currentPage !== totalPages) {

        const nextLi =
            document.createElement("li");

        const nextButton =
            document.createElement("button");

        nextButton.textContent =
            "Próximo »";

        nextButton.dataset.action =
            "next";

        nextLi.appendChild(nextButton);

        pageButtons.appendChild(nextLi);
    }
}

/* =========================
   EVENTO DOS BOTÕES
========================= */

document.querySelector('.pagination')
    .addEventListener('click', (event) => {

        const botao =
            event.target.closest('button');

        if (!botao) return;

        const acao =
            botao.dataset.action;

        if (acao === 'prev') {

            currentPage--;

        } else if (acao === 'next') {

            currentPage++;

        } else {

            currentPage =
                parseInt(botao.textContent);
        }

        sendQuery(currentPage);
    });