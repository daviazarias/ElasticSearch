function sendQuery() {
    const query = document.getElementById('query').value;
    const queryURL = encodeURIComponent(query);
    const url = document.URL + "search?query=" + queryURL;
    fetch(url)
        .then(response => {
            if(!response.ok) {
                throw new Error('Não foi ok :(');
            }
            return response.json();
        })
        .then(data => showResults(data))
        .catch(error => console.error('Fetch error:', error));
}

const input = document.querySelector("input");
input.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
        event.preventDefault();
        sendQuery();
    }
});

const resultsList = document.getElementById("results")
function showResults(data) {
    resultsList.replaceChildren();

    for (let result of data.results) {
        var tituloContainer = document.createElement("dt");

        var link = document.createElement("a");
        link.textContent = result.title;
        link.href = result.url;
        link.target = "_blank";
        tituloContainer.appendChild(link);

        var abstractContainer = document.createElement("dd");
        abstractContainer.textContent = result.abs;

        resultsList.appendChild(tituloContainer);
        resultsList.appendChild(abstractContainer);
    }
}