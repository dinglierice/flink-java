function  test() {
    c = ["1", "2", "3"]
    c.forEach(element => {
        if (element === "1") {
        }
    });
    return JSON.stringify({
        "a":1,
        "b":2,
        "c":[
            {"tt":"fa", "cc":"fb"},
            {"tt":"fa1", "cc":"fb1"}
        ]
    })
}
