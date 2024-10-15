# custom-http-plugin

Capacitor http with progress event

## Install

```bash
npm install custom-http-plugin
npx cap sync
```

## API

<docgen-index>

* [`post(...)`](#post)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### post(...)

```typescript
post<T>(data: HttpInfo) => Promise<T>
```

| Param      | Type                                          |
| ---------- | --------------------------------------------- |
| **`data`** | <code><a href="#httpinfo">HttpInfo</a></code> |

**Returns:** <code>Promise&lt;T&gt;</code>

--------------------


### Interfaces


#### HttpInfo

| Prop          | Type                                                         |
| ------------- | ------------------------------------------------------------ |
| **`url`**     | <code>string</code>                                          |
| **`data`**    | <code><a href="#record">Record</a>&lt;string, any&gt;</code> |
| **`files`**   | <code>{ fileName: string; base64Data: string; }</code>       |
| **`headers`** | <code>{ Authorization: string; }</code>                      |


### Type Aliases


#### Record

Construct a type with a set of properties K of type T

<code>{ [P in K]: T; }</code>

</docgen-api>
