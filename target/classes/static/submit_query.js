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
        .then(data => console.log(data))
        .catch(error => console.error('Fetch error:', error));
}

const input = document.querySelector("input");
input.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
        event.preventDefault();
        sendQuery();
    }
});