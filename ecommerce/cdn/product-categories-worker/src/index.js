export default {
  async fetch(request) {
    const categories = [
        [
		  { "categoryId": 1, "name": "Electronics" },
		  { "categoryId": 2, "name": "Beauty" },
		  { "categoryId": 3, "name": "Car" },
		  { "categoryId": 4, "name": "Furniture" },
		  { "categoryId": 5, "name": "Toys" },
		  { "categoryId": 6, "name": "Watches" }
		]
    ];
    return new Response(JSON.stringify(categories), {
      headers: { "Content-Type": "application/json" }
    });
  }
}
